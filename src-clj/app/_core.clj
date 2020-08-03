(ns app._core
  (:use hiccup.core)
  (:require [app.db :as db]
            [next.jdbc :as next]
            [clojure.java.jdbc]
            [app.games :as games]
            [app.routes :as routes])
  )

(defn r []
  "repl helper except doesn't always work. tries to reload all code."
  (use 'app._core :reload-all))

(def here? "_core.clj")

(defn main []
  (println "Main..."))

