(ns akvo.lumen.endpoint.resource
  (:require [akvo.lumen.component.tenant-manager :refer [connection]]
            [akvo.lumen.lib.resource :as resource]
            [compojure.core :refer :all]))


(defn endpoint [{:keys [tenant-manager]}]
  (context "/api/resources" {:keys [params tenant] :as request}
    (let-routes [tenant-conn (connection tenant-manager tenant)]

      (GET "/" _
        (resource/all tenant-conn)))))
