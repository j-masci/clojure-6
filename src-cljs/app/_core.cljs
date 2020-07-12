(ns app._core
  (:require
    [app.game :as game]
    [reagent.core :as r]
    [reagent.dom :as rd]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rd/render [game/app-component] (js/document.getElementById "app")))

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