(ns _core
  (:require
    [components :as c]
    [reagent.core :as r]
    [reagent.dom :as rd]))

(js/console.log "_core.cljs" 2333)

;; -------------------------
;; Initialize app

(defn mount-root []
  (rd/render [c/app] (js/document.getElementById "app")))

(defn init! []
  (mount-root))

(.addEventListener
  js/window
  "DOMContentLoaded"
  init!)

;(defn ^:dev/before-load stop []
;      (js/console.log "stop"))

;(defn ^:dev/after-load start []
;      (js/console.log "start"))