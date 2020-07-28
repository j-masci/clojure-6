(ns app.endpoints
  "API endpoints"
  (:require [app.db :as db]
            [utils.strings :as strings]
            [app.games :as games]))

(defn games-get
  "/api/games/get?id=123"
  [request]
  (games/serialize-game-for-api (games/get-or-insert-via-public-id (get-in request [:params "id"]))))
