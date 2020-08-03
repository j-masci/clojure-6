(ns app.db
  "Contains many database related things including some aliases for functions
  in other namespaces. Contains:

  - database table definitions
  - database migration functions
  - querying functions"
  (:require [app.utils.core]
            [app.utils.sql]
            [app.utils.next-jdbc-wrapper :as next-jdbc-wrapper]
            [honeysql.core :as honeysql]
            [next.jdbc.result-set :as next.result-set]
            [clojure.java.jdbc]))

(def spec
  "Often called `connectable` in next.jdbc functions."
  {:dbtype "h2"
   :dbname "./h2-db"})

(def opts
  "Default options passed to execute/execute-one/insert/update/delete functions."
  {:builder-fn next.result-set/as-lower-maps})

; ie. (execute! ["SELECT * FROM table WHERE col = ?" 10] {:option-override "..."})
(def execute! (next-jdbc-wrapper/make-fn__execute! spec opts))

; like execute! but for statements which return a single value (or row) (such as insert, create table, etc.)
(def execute-one! (next-jdbc-wrapper/make-fn__execute-one! spec opts))

; ie. (insert! :table-name {:col-1 "val-1"})
(def insert! (next-jdbc-wrapper/make-fn__insert! spec opts))

; ie. (update! :table-name {:col-1 10} {:col-1 "new column value"})
(def update! (next-jdbc-wrapper/make-fn__update! spec opts))

; ie. (delete! :table-name {:col-1 10})
(def delete! (next-jdbc-wrapper/make-fn__delete! spec opts))

(defn get-where
  "Wraps app.utils.sql/get-where but executes the statement returned from there."
  [& args] (execute! (apply app.utils.sql/get-where args)))

(def tables
  "Database table names and column specifications."
  {:games       {:primary-key  :game_id
                 :column-specs [[:game_id "BIGINT NOT NULL AUTO_INCREMENT"]
                                [:public_id "text"]
                                [:state "text"]]}

   :keyvals     {:primary-key  :keyval_id
                 :column-specs [[:keyval_id "BIGINT NOT NULL AUTO_INCREMENT"]
                                [:key "text"]
                                [:value "text"]]}

   :ent_keyvals {:primary-key  :ent_keyval_id
                 :column-specs [[:ent_keyval_id "BIGINT NOT NULL AUTO_INCREMENT"]
                                [:type "varchar(255)"]
                                [:ent_id "bigint"]
                                [:key "text"]
                                [:value "text"]]}
   })

(defn table-exists
  "True for tables that get registered above."
  [table]
  (contains? tables table))

(defn get-primary-key
  "Get a primary key via table name (keyword)."
  [table]
  (get-in tables [table :primary-key]))

(defn get-via-pk
  "Query a database table based on the value of its primary key.

  Returns a vector of (likely 0 or 1) map."
  [table pk-value]
  (get-where table (get-primary-key table) pk-value))

(defn create-table-ddls
  "Generate the SQL to create all tables.

  {:conditional? true} => add \"if not exists\""
  []
  (for [[table_name table_data] tables]
    (clojure.java.jdbc/create-table-ddl table_name (:column-specs table_data) {:conditional? true})))

(defn drop-table-ddls []
  "Generate the SQL to drop all tables."
  (for [[table_name _] tables]
    (clojure.java.jdbc/drop-table-ddl table_name {:conditional? true})))

(defn create-tables!
  "Create all database tables."
  []
  (for [ddl (create-table-ddls)]
    (do
      (println "Attempting:" ddl)
      (println "Result:")
      (println (execute-one! (vector ddl))))))

(defn drop-tables!
  "Drop the entire database!"
  []
  (for [ddl (drop-table-ddls)]
    (do
      (println "Attempting:" ddl)
      (println "Result:")
      (println (execute-one! (vector ddl))))))

(defn re-create-tables!
  "Drop and then create all database tables."
  []
  (drop-tables!)
  (create-tables!))