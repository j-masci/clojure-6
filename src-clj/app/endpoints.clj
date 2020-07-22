(ns app.endpoints
  "API endpoints"
  (:require [app.db :as db]
            [utils.strings :as strings]))

(def hi (db/query ["SELECT 5"]))

(defn games-get [request]
  (let [id (get-in request [:params "id"])
        games (db/get-where :games :public_id id)]
    (if (empty? games) (db/insert! :games {:public_id id}))
    (first (db/get-where :games :public_id id))))
