(ns app.views "Views for the app (probably just the single page)"
  (:use hiccup.page)
  (:require [app.config :as config]
            [clojure.data.json :as json]))

(defn- head
  "HTML <head> tag"
  []
  [:head
   [:title "Clojure 6"]
   [:script {:type "text/javascript"} (let [m {:base_url (:base-url config/env)}
                                            json* (with-out-str (json/pprint m))]
                                        (str "window.app_config = " json* ";"))]
   (include-js "cljs-out/dev-main.js")
   (include-css "css/app.css")])

(defn index
  "The single page."
  [request]
  (let []
    (html5 {:lang "en"} (head) [:body [:div#app.app]])))