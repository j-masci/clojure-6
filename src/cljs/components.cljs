(ns components
  (:require [reagent.core :as r]
    ; [clojure.string :as string]
            ))

(def state (r/atom {:idk     1
                    :counter 0}))

(defn manual-re-render []
  (swap! state update :counter inc))

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
  [:div.console
   (let [i (map vector (range (count items-vec)) items-vec)]
     (map #(vector :p (str (% 0) ": " (str (% 1)))) (reverse i)))])

(defn app []
  (log "app render")
  [:div.app-inner
   [:h1 "Game"]
   [:div.state (str @state)]
   [:div.btns
    [btn "hi" (fn [e] (swap! state update :idk inc))]
    [btn "bye" (fn [] (log "Bye") (manual-re-render))]]
   [console @-console-vec]
   [:br]
   [board]])
