(ns _core
  (:require [db]))

(defn reload
  "For repl"
  []
  (use '_core :reload))

(defn main []
  (println "Main..."))

