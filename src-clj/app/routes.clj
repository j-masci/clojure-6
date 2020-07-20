(ns app.routes
  (:use hiccup.page)
  (:require [clojure.pprint]
            [app.config :as config]
            [clojure.data.json :as json]
            [compojure.core :as cmp]
            [compojure.route :as route]))

(defn- head []
  [:head
   [:title "Clojure 6"]
   [:script {:type "text/javascript"} (let [m {:base_url (:base-url config/env)}
                                            json* (with-out-str (json/pprint m))]
                                        (str "window.app_config = " json* ";"))]
   (include-js "cljs-out/dev-main.js")
   (include-css "css/app.css")])

(defn index-page [request]
  (let []
    (html5 {:lang "en"} (head) [:body [:div#app.app]])))

(cmp/defroutes app
               (cmp/GET "/" [] index-page)
               (route/resources "/" {:root "public"})
               (route/not-found "Not found."))