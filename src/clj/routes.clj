(ns routes
  (:use hiccup.page)
  (:require [clojure.pprint]
            [compojure.core :as cmp]
            [compojure.route :as route]))

(defn- head []
  [:head
   [:title "Clojure 6"]
   (include-js "js/main.js")
   (include-css "css/master.css")])

(defn index [request]
  (let []
    (html5 {:lang "en"} (head) [:body {:class "body" :id "body"} [:div#app]])))

(cmp/defroutes app
               (cmp/GET "/" [] index)
               (route/resources "/" {:root "public"})
               (route/not-found "Not found."))