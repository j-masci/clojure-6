(ns app._core
  (:use hiccup.core)
  (:require [app.db :as db]
            [app.games :as games]
            [app.routes :as routes])
  )

(def here? "_core.clj")

(defn main []
  (println "Main..."))

