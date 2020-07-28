(ns app.db
  (:require [utils.core :as utils]
            [honeysql.core :as honeysql]
            [next.jdbc :as next]
            [next.jdbc.result-set :as next.result-set]
            [next.jdbc.sql :as next.sql]
    ; used for a few things which next.jdbc does not provide:
            [clojure.java.jdbc :as _jdbc]
            ))

(def spec {:dbtype "h2"
           :dbname "./h2-db"})

(def default-opts
  "Default options injected into certain aliases for next.jdbc functions
  which are defined in this file."
  {:builder-fn next.result-set/as-lower-maps})

(def tables
  {:games       {:pk    :game_id
                 :specs [[:game_id "BIGINT NOT NULL AUTO_INCREMENT"]
                         [:public_id "text"]
                         [:state "text"]]}

   :keyvals     {:pk    :keyval_id
                 :specs [[:keyval_id "BIGINT NOT NULL AUTO_INCREMENT"]
                         [:key "text"]
                         [:value "text"]]}

   :ent_keyvals {:pk    :ent_keyval_id
                 :specs [[:ent_keyval_id "BIGINT NOT NULL AUTO_INCREMENT"]
                         [:type "varchar(255)"]
                         [:ent_id "bigint"]
                         [:key "text"]
                         [:value "text"]]}
   })

(defn table-exists
  "True for tables that get registered above."
  [table]
  (contains? tables table))

(defn get-pk
  "Get a primary key via table name (keyword)."
  [table]
  (let [ret (get-in tables [table :pk])]
    (assert (table-exists table))
    ret))

(defn execute!
  "Wraps next/execute!, injects db specs and default options."
  ([sql-params] (execute! sql-params {}))
  ([sql-params opts]
   (next/execute! spec sql-params (merge default-opts opts))))

; idk why, but i'm just going to do this.
(def query execute!)

(defn execute-one!
  "Wraps next/execute-one!, injects db specs and default options."
  ([sql-params] (execute-one! sql-params {}))
  ([sql-params opts]
   (next/execute-one! spec sql-params (merge default-opts opts))))

;(comment
;  (some-magic execute! next/execute!
;              {:1 #(Math/abs %)}))

(defn f
  ([a] (f a 10))
  ([a b] (println a b)))

(defn next-jdbc-make-insert! [default-opts]
  (letfn [(f
            ([connectable table key-map] (f connectable table key-map {}))
            ([connectable table key-map opts] (next.sql/insert! connectable table key-map (merge default-opts opts))))] f))

(defn fn-alias-with-args-filter
  "Pass in a function f and a function args-filter,
  returns a function that invokes f but with the vector
  of arguments that args-filter returns. args-filter accepts
  and must return a vector of function arguments."
  [f args-filter] (fn [& args] (apply f (args-filter (into [] args)))))

(def insert! (fn-alias-with-args-filter next.sql/insert! (fn [args] (condp = (count args)
                                                                      2 (cons spec args)
                                                                      3 (vector)))))

(defn insert! (partial (next-jdbc-make-insert! default-opts) spec))

(defn insert!
  ""
  ([table key-map] (insert! table key-map {}))
  ([table key-map opts] (next.sql/insert! spec table key-map (merge default-opts opts))))

; ie. (insert! :table {:col1 42 :col2 "123"})
(def insert! (partial next.sql/insert! spec))

; ie. (update! :table {:col1 77 :col2 "456"} ["id = ?" 13])
(def update! (partial next.sql/update! spec))

; ie. (delete! :table ["id = ?" 13])
(def delete! (partial next.sql/delete! spec))

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
     (execute! sql-params)))
  ([table column value]
   (get-where table (hash-map column value))))

(defn get-via-pk [table pk-value]
  (let [r (get-where table (get-pk table) pk-value)]
    (if (empty? r) nil (r 0))))

; ie. index
; (def -after-create {:keyvals ""})

(defn create-table-ddls []
  (for [[table_name table_data] tables]
    (_jdbc/create-table-ddl table_name (:specs table_data) {:conditional? true})))

(defn drop-table-ddls []
  (for [[table_name _] tables]
    (_jdbc/drop-table-ddl table_name {:conditional? true})))

(defn create-tables! []
  (run! #(utils/with-try-catch (println "Attempting: " %1 ". \r\n Result: " (execute! %1)) (fn [e] (println (.getMessage e)))) (create-table-ddls)))

(defn drop-tables! []
  (run! #(utils/with-try-catch (println "Attempting: " %1 ". \r\n Result: " (execute! %1)) (fn [e] (println (.getMessage e)))) (drop-table-ddls)))

(defn re-create-tables! []
  (drop-tables!)
  (create-tables!))