(ns endpoints
  "API endpoints"
  (:require [db]
            [sanitize]))

(def hi (db/query ["SELECT 5"]))

(defn game [request]
  (let [id (get-in request [:params "id"])]
    {:id id}))
