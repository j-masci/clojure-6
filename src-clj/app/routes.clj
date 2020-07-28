(ns app.routes
  (:use hiccup.page)
  (:require [app.views :as views]
            [app.endpoints :as endpoints]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [compojure.core :as cmp]
            [compojure.route :as route]
            [ring.middleware.params :as params]))

(defn with-mw
  "Default middleware for non API requests, ie. the home page."
  [handler]
  (-> handler
      (params/wrap-params)))

(defn with-api-mw
  "Wraps a route handler in middleware intended for API requests."
  [handler]
  (-> handler
      (params/wrap-params)))

(defn decorate
  "Decorate a non API handler."
  [handler]
  (fn [request]
    (let [response ((with-mw handler) request)]
      response)))

(defn decorate-api
  "Decorate an API handler, adding middleware, and formatting
  the return value."
  [handler]
  (fn [request]
    (let [response ((with-api-mw handler) request)]
      {:status 200
       :body (with-out-str (json/pprint response))})))

; probably going to have to refactor the middleware/decorating thing
(cmp/defroutes app
               (cmp/GET "/" [] (decorate views/index))
               (cmp/GET "/api/games/get" [] (decorate-api endpoints/games-get))
               (route/resources "/" {:root "public"})
               (route/not-found "Not found."))