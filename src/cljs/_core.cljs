(ns _core
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]))

(js/console.log "_core.cljs" 2333)

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Screw You"]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (js/document.getElementById "app")))

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