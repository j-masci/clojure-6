(ns db
  (:require [utils]
            [clojure.java.jdbc :as jdbc]))

(def -pk "BIGINT NOT NULL AUTO_INCREMENT")

(def spec {:dbtype "h2"
           :dbname "./h2-db"})

; can use these aliases without manually injecting the db specs



; ie. (query ["SELECT * FROM table WHERE id = ?" 13])
(def query (partial jdbc/query spec))

; ie. for drop/create tables or statements that are not SELECT
(def execute! (partial jdbc/execute! spec))

; ie. (insert! :table {:col1 42 :col2 "123"})
(def insert! (partial jdbc/insert! spec))

; ie. (update! :table {:col1 77 :col2 "456"} ["id = ?" 13])
(def update! (partial jdbc/update! spec))

; ie. (delete! :table ["id = ?" 13])
(def delete! (partial jdbc/delete! spec))

(def tables {:games       [[:game_id -pk]
                           [:public_id "longtext"]
                           [:state "longtext"]]

             :keyvals     [[:keyval_id -pk]
                           [:key "longtext"]
                           [:value "longtext"]]

             :ent_keyvals [[:ent_keyval_id -pk]
                           [:type "varchar(255)"]
                           [:ent_id "bigint"]
                           [:key "longtext"]
                           [:value "longtext"]]})

; ie. index
(def -after-create {:keyvals ""})

(defn create-table-ddls []
  (for [[table specs] tables] (jdbc/create-table-ddl table specs)))

(defn drop-table-ddls []
  (for [[table specs] tables] (jdbc/drop-table-ddl table specs)))

(defn create-tables! []
  (run! #(utils/try-catch-print (fn [] (execute! %))) (create-table-ddls)))

(defn drop-tables! []
  (run! #(utils/try-catch-print (fn [] (execute! %))) (drop-table-ddls)))

(defn re-create-tables! []
  (drop-tables!)
  (create-tables!))