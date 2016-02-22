(ns org.akvo.dash.integration-test
  (:require
   [org.akvo.dash.fixtures :refer [integration-fixture]]
   [clj-http.client :as client]
   [clojure.test :refer :all]))


(use-fixtures :once integration-fixture)

(deftest ^:integration ping

  (testing "Root endpoint - status code"
    (let [resp (client/get "http://localhost:3000/api")]
      (is (= 200 (:status resp)))))

  (testing "Dataset endpoint - status code"
    (let [resp (client/get "http://localhost:3000/api/datasets")]
      (is (= 200 (:status resp)))))

  (testing "Library endpoint - status code"
    (let [resp (client/get "http://localhost:3000/api/library")]
      (is (= 200 (:status resp))))))


(deftest ^:wip ping-wip

  (testing "Dataset endpoint - status code"
    (let [resp (client/get "http://localhost:3000/api/datasets/does-not-exists")]
      (is (= 404 (:status resp)))))

  )
