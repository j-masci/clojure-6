(ns games
  (:require [db]))

(defn game-exists? [id]
  (not (empty? (db/query "SELECT game_id FROM games WHERE public_id = ?" id))))