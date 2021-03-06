(ns akvo.lumen.import.flow-v2
  (:require [akvo.commons.psql-util :as pg]
            [akvo.lumen.import.common :as import]
            [akvo.lumen.import.flow-common :as flow-common]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str])
  (:import [java.time Instant]))


(defn question-type->lumen-type
  [question]
  (condp = (:type question)
    "NUMBER" :number
    "DATE" :date
    :text))

(defn dataset-columns
  [form version]
  (let [questions (flow-common/questions form)]
    (into
     [(cond-> {:title "Instance id" :type :text :id :instance_id}
        (<= 2 version) (assoc :key true))
      (let [identifier {:title "Identifier" :type :text :id :identifier}]
        (if (and (:registration-form? form) (<= 2 version))
          (assoc identifier :key true)
          identifier))
      {:title "Display name" :type :text :id :display_name}
      {:title "Latitude" :type :number :id :latitude}
      {:title "Longitude" :type :number :id :longitude}
      {:title "Submitter" :type :text :id :submitter}
      {:title "Submitted at" :type :date :id :submitted_at}
      {:title "Surveyal time" :type :number :id :surveyal_time}]
     (map (fn [question]
            {:title (:name question)
             :type (question-type->lumen-type question)
             :id (keyword (format "c%s" (:id question)))})
          questions))))

(defmulti render-response
  (fn [type response]
    type))

(defmethod render-response "DATE"
  [_ response]
  (Instant/parse response))

(defmethod render-response "FREE_TEXT"
  [_ response]
  response)

(defmethod render-response "NUMBER"
  [_ response]
  response)

(defmethod render-response "OPTION"
  [_ response]
  (str/join "|" (map (fn [{:strs [text code]}]
                       (if code
                         (str/join ":" [code text])
                         text))
                     response)))

(defmethod render-response "GEO"
  [_ response]
  (condp = (get-in response ["geometry" "type"])
    "Point" (let [coords (get-in response ["geometry" "coordinates"])]
              (str/join "," coords))
    nil))

(defmethod render-response "CASCADE"
  [_ response]
  (str/join "|" (map (fn [item]
                       (get item "name"))
                     response)))

(defmethod render-response "PHOTO"
  [_ response]
  (get response "filename"))

(defmethod render-response "VIDEO"
  [_ response]
  (get response "filename"))

(defmethod render-response "CADDISFLY"
  [_ response]
  (json/generate-string response))

(defmethod render-response :default
  [type response]
  nil)

(defn response-data
  [form responses]
  (let [responses (flow-common/question-responses responses)]
    (reduce (fn [response-data {:keys [type id]}]
              (if-let [response (get responses id)]
                (assoc response-data
                       (keyword (format "c%s" id))
                       (render-response type response))
                response-data))
            {}
            (flow-common/questions form))))

(defn form-data
  "Returns a lazy sequence of form data, ready to be inserted as a lumen dataset"
  [headers-fn survey form-id]
  (let [form (flow-common/form survey form-id)
        data-points (flow-common/index-by "id" (flow-common/data-points headers-fn survey))]
    (map (fn [form-instance]
           (let [data-point-id (get form-instance "dataPointId")]
             (assoc (response-data form (get form-instance "responses"))
                    :instance_id (get form-instance "id")
                    :display_name (get-in data-points [data-point-id "displayName"])
                    :identifier (get-in data-points [data-point-id "identifier"])
                    :latitude (get-in data-points [data-point-id "latitude"])
                    :longitude (get-in data-points [data-point-id "longitude"])
                    :submitter (get form-instance "submitter")
                    :submitted_at (some-> (get form-instance "submissionDate")
                                          Instant/parse)
                    :surveyal_time (get form-instance "surveyalTime"))))
         (flow-common/form-instances headers-fn form))))
