(ns app.db
  (:require [utils.core :as utils]
            [clojure.java.jdbc :as jdbc]))

(def spec {:dbtype "h2"
           :dbname "./h2-db"})

(def -pk-ddl "BIGINT NOT NULL AUTO_INCREMENT")

(def tables
  {:games       {:pk    :game_id
                 :specs [[:game_id -pk-ddl]
                         [:public_id "text"]
                         [:state "text"]]}

   :keyvals     {:pk    :keyval_id
                 :specs [[:keyval_id -pk-ddl]
                         [:key "text"]
                         [:value "text"]]}

   :ent_keyvals {:pk    :ent_keyval_id
                 :specs [[:ent_keyval_id -pk-ddl]
                         [:type "varchar(255)"]
                         [:ent_id "bigint"]
                         [:key "text"]
                         [:value "text"]]}
   })

(defn get-pk
  "Get a primary key via table name (keyword)."
  [table]
  (get-in tables [table :pk]))

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

(defn sanitize-table-col
  "Sanitize table or columns string to make it safe to use directly
  in an SQL query. Happens to also be useful for when we pass tables/columns
  as keywords, as it will drop the leading colon.

  Allows dots for input such as \"table_name.column_name\".

  The main concern here is security, not whether or not table names
  alone are allowed to have dots in them or start with a number."
  [s]
  (clojure.string/replace s #"[^a-zA-Z0-9_.]" ""))

(defn map->keys-and-vals
  "From a map, returns a vector of two vectors, which include
  the keys and the values."
  [m]
  (let [v (into [] m)]
    [(reduce #(conj %1 (%2 0)) [] v)
     (reduce #(conj %1 (%2 1)) [] v)]))

(defn get-where
  "Runs a select statement on a table with equality conditions specified in a map."
  ([table where]
   (assert (map? where))
   (let [[keys vals] (map->keys-and-vals where)
         where-str (clojure.string/join " AND " (mapv #(str (sanitize-table-col %1) " = ?") keys))
         sql-params (into [] (concat [(str "SELECT * FROM " (sanitize-table-col table) " WHERE " where-str)] vals))]
     (query sql-params)))
  ([table column value]
   (get-where table (hash-map column value))))

(defn get-via-pk [table pk-value]
  (let [r (get-where table (get-pk table) pk-value)]
    (if (empty? r) nil (r 0))))

; ie. index
; (def -after-create {:keyvals ""})

(defn create-table-ddls []
  (for [[table_name table_data] tables]
    (jdbc/create-table-ddl table_name (:specs table_data))))

(defn drop-table-ddls []
  (for [[table_name table_data] tables]
    (jdbc/drop-table-ddl table_name (:specs table_data))))

(defn create-tables! []
  (run! #(utils/with-try-catch (execute! %1) (fn [e] (println (.getMessage e)))) (create-table-ddls)))

(defn drop-tables! []
  (run! #(utils/with-try-catch (execute! %1) (fn [e] (println (.getMessage e)))) (drop-table-ddls)))

(defn re-create-tables! []
  (drop-tables!)
  (create-tables!))