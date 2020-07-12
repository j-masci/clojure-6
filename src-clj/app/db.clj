(ns app.db
  (:require [clojure.java.jdbc :as jdbc]))

(def -pk "bigint NOT NULL auto_increment")

(def spec {:dbtype   "h2"
           :dbname   "./h2-db"})

(defn query []
  (partial jdbc/query spec))

(def tables {:keyvals     [[:keyval_id -pk]
                           [:key "longtext"]
                           [:value "longtext"]]

             :ent_keyvals [[:ent_keyval_id -pk]
                           [:type "varchar(255)"]
                           [:ent_id "bigint"]
                           [:key "longtext"]
                           [:value "longtext"]]})

; ie. index
(def -after-create {:keyvals ""})

(defn create-tables! []
  "Executes create_table on all tables. (not going to be good if some already exist)."
  (doseq [table-vec tables]
    (let [ddl (jdbc/create-table-ddl (table-vec 0) (table-vec 1))]
      (jdbc/db-do-commands spec ddl))))