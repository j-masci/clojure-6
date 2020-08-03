(ns app.utils.next-jdbc-wrapper
  "Wraps the next.jdbc public API in a way that's a bit more convenient to use.

  Contains higher order functions which you can use to alias the public
  API in your own namespace.

  (todo...) In hindsight, the functions should not accept the database specs
  because this could have been done very easily with `partial`
  "
  (:require [next.jdbc :as next]
            [next.jdbc.sql :as next.sql]
            [next.jdbc.result-set :as next.result-set]))

(def example-default-opts
  {:builder-fn next.result-set/as-lower-maps})

(defn make-fn__execute! [spec default-opts]
  "Returns a function that wraps next/execute!"
  (letfn [(execute!
            ([sql-params] (execute! sql-params {}))
            ([sql-params opts]
             (next/execute! spec sql-params (merge default-opts opts)))
            )] execute!))

(defn make-fn__execute-one! [spec default-opts]
  "Returns a function that wraps next/execute-one!"
  (letfn [(execute-one!
            ([sql-params] (execute-one! sql-params {}))
            ([sql-params opts]
             (next/execute-one! spec sql-params (merge default-opts opts)))
            )] execute-one!))

(defn make-fn__insert! [spec default-opts]
  "Returns a function that wraps next.sql/insert!"
  (letfn [(insert!
            ([table key-map] (insert! table key-map {}))
            ([table key-map opts] (next.sql/insert! spec table key-map (merge default-opts opts)))
            )] insert!))

(defn make-fn__update! [spec default-opts]
  "Returns a function that wraps next.sql/update!"
  (letfn [(update!
            ([table key-map where-params] (update! table key-map where-params {}))
            ([table key-map where-params opts] (next.sql/update! spec table key-map where-params (merge default-opts opts)))
            )] update!))

(defn make-fn__delete! [spec default-opts]
  "Returns a function that wraps next.sql/delete!"
  (letfn [(delete!
            ([table where-params] (delete! table where-params {}))
            ([table where-params opts] (next.sql/delete! spec table where-params (merge default-opts opts)))
            )] delete!))
