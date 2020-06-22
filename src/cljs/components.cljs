(ns components
  (:require [reagent.core :as r]
    ; [clojure.string :as string]
            ))

(def state (r/atom {:idk     1
                    :console []}))

(def -console-vec (atom []))

(defn log [& args]
  (swap! -console-vec conj args))

(defn btn [text callback]
  [:button {:style {:margin "10px" :display "inline-block"} :on-click callback} text])

(defn board []
  [:div.board
   {:style {:width "500px" :height "500px" :margin "0 auto" :background "grey"}}])

(defn console [items-vec]
  "on page console for debugging"
  (js/console.log items-vec)
  [:div {:style {:border "1px solid grey" :padding "10px" :max-height "200px" :overflow "auto"}}
   (mapv #(vector :p (str %)) items-vec)])

(defn test1 [a]
  [:p (str a (:idk @state))])

(defn app []
  [:div.app-inner
   [:h1 (do (log "App render") "Game")]
   [:div.state @state]
   [:div.console "324"]
   ; [:div.state @-console-vec]
   ; [console @-console-vec]
   [test1 25]
   [:div.btns
    [btn "hi" (fn [e] (swap! state update :idk inc))]
    [btn "bye" identity]]
   [board]])
