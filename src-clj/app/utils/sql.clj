(ns app.utils.sql)

(defn map->keys-and-vals
  "From a map, returns a vector of two vectors, which include
  the keys and the values."
  [m]
  (let [v (into [] m)]
    [(reduce #(conj %1 (%2 0)) [] v)
     (reduce #(conj %1 (%2 1)) [] v)]))

(defn sanitize-table-col
  "Sanitize table or columns string to make it safe to use directly
  in an SQL query. Happens to also be useful for when we pass tables/columns
  as keywords, as it will drop the leading colon.

  Allows dots for input such as \"table_name.column_name\".

  The main concern here is security, not whether or not table names
  alone are allowed to have dots in them or start with a number."
  [s]
  (clojure.string/replace s #"[^a-zA-Z0-9_.]" ""))

(defn get-where
  "Returns an sql SELECT statement (as an sql-params vector) generated from
  where conditions passed in as a map."
  ([table where]
   (assert (map? where))
   (let [[keys vals] (map->keys-and-vals where)
         where-str (clojure.string/join " AND " (mapv #(str (sanitize-table-col %1) " = ?") keys))
         sql-params (into [] (concat [(str "SELECT * FROM " (sanitize-table-col table) " WHERE " where-str)] vals))]
     sql-params))
  ([table column value]
   (get-where table (hash-map column value))))

