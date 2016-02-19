(ns org.akvo.dash.endpoint.library
  "Library endpoint..."
  (:require
   [camel-snake-kebab.core :refer [->kebab-case-keyword ->snake_case_string]]
   [cheshire.core :as json]
   [clojure.pprint :refer [pprint]]
   [compojure.core :refer :all]
   [hugsql.core :as hugsql]
   ))

(hugsql/def-db-fns "org/akvo/dash/endpoint/library.sql")

(defn endpoint
  ""
  [{{db :spec} :db}]
  (context "/library" []


    (GET "/" []
      (fn [req]
        (let [datasets       (dataset-coll db)
              visualisations []
              dashboards     []]
          {:status  200
           :headers {"content-type" "application/json"}
           :body    (json/generate-string
                     {:datasets       datasets
                      :visualisations visualisations
                      :dashboards     dashboards}
                     {:key-fn (fn [k] (->snake_case_string k))})})))))
