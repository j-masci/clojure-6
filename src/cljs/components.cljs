(ns components
  (:require [reagent.core :as r]))

(def state (r/atom {:idk     []
                    :counter 0
                    :board-size 7}))

(defn rand-pieces []
  [{:type "Rk"
    :owner 0
    :pos [3 4]}
   {:type "Bs"
    :owner 1
    :pos [5 5]}
   {:type "Kn"
    :owner 1
    :pos [0 1]}])

(defn manual-re-render []
  (swap! state update :counter inc))

(def -console-vec (atom []))

(defn log "Adds text to on-screen console" [& args]
  (swap! -console-vec conj args))

(defn btn [text callback]
  [:button {:style {:margin "10px" :display "inline-block"} :on-click callback} text])

(defn state-updater [key] [:input {:type "number" :on-change (fn [e] (swap! state assoc key (-> e .-target .-value))) :value (@state key)}])

(defn board-square-bottom-left-pct [count total]
  (if (= 0 count) 0 (* 100 (/ count total))))

(defn board-square [x y x-count y-count]
  (let [w (* 100 (/ 1 x-count))
        l (board-square-bottom-left-pct x x-count)
        b (board-square-bottom-left-pct y y-count)] [:div.board-square
           {:key (str x y)
            :style {:width (str w "%")
                    :height (str w "%")
                    :left (str l "%")
                    :bottom (str b "%")
                    :background (if (even? (+ x y)) "white" "lightGrey")}}
           [:div.board-square-inner [:p.coord (str x ", " y)]]]))

(defn board-squares [x-count y-count]
  [:div.board-squares {:style {}}
   (for [x (range x-count) y (range y-count)]
     [board-square x y x-count y-count])])

(defn board-component [{:keys [x-count y-count pieces]}]
  [:div.board (list
                [board-squares x-count y-count])])

(defn console [items-vec]
  "on page console for debugging"
  (js/console.log items-vec)
  [:div.console
   (let [i (map vector (range (count items-vec)) items-vec)]
     (map #(vector :p {:key i} (str (% 0) ": " (str (% 1)))) (reverse i)))])

(defn app []
  (log "app render")
  [:div.app-inner
   [:h1 "Game"]
   [:div.state (str @state)]
   [:div.btns
    [btn "hi" #(swap! state conj :idk "Hi")]
    [btn "Draw" #(swap! state conj :idk "Hi")]
    [btn "bye" #((log "Bye") (manual-re-render))]]
   [state-updater :board-size]
   [console @-console-vec]
   [:br]
   [board-component {:x-count (:board-size @state) :y-count (:board-size @state) :pieces []}]])
