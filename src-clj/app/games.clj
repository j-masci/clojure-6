(ns app.games
  (:require [honeysql.core :as sql]
            [clojure.data.json :as json]
            [app.db :as db]))

(defn get-or-insert-via-public-id [public-id]
  (let [games (db/get-where :games :public_id public-id)]
    (if (empty? games)
      (do (db/insert! :games {:public_id public-id})
          (first (db/get-where :games :public_id public-id)))
      (first games))))

(defn serialize-game-for-api
  [row-from-sql-result]
  ())

(defn game-exists? [id]
  (not (empty? (db/execute! ["SELECT game_id FROM games WHERE public_id = ?" id]))))