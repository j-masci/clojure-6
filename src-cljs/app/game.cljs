(ns app.game
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            ;[oops.core :refer [oget oset! ocall oapply ocall! oapply!
            ;                   oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            ))

(enable-console-print!)

(defn endpoint [e] (str "http://localhost:9501/api/" e))

(.log js/console "fuck you")
(.log js/console (http/get (endpoint "game")))

;;;;;;;;;;;;;; config

(def grid-num 5)

(def draw-frequency 2000)

;;;;;;;;;;; app-state

(defonce ^:dynamic *canvas* (atom nil))

(defonce app-state (r/atom {:idk     []
                            :counter 0
                            :desired-game-id 0
                            :game-id 0}))

(defn set-state [key value]
  (swap! app-state assoc key value))

(defn get-state
  ([key] (get-state key nil))
  ([key default]
   (get @app-state key default)))

(defn set-in-state [keys-vec value]
  (swap! app-state assoc-in keys-vec value))

(defn get-in-state
  ([keys-vec] (get-in-state keys-vec nil))
  ([keys-vec default]
   (get-in @app-state keys-vec default)))

;;;;;;;;;;;;; canvas stuff

(defn world-to-canvas-coords
  "Changes the y-coordinate of a 2d vector in preparation for
  drawing on the canvas, where the origin is at the top left.

  (world-to-canvas-coords [0 0] 500) => [0 500]"
  [point canvas-height]
  (vector (point 0) (- canvas-height (point 1))))

(defn draw-line!
  ([canvas x1 y1 x2 y2] (draw-line! canvas x1 y1 x2 y2 {}))
  ([canvas x1 y1 x2 y2 opts]
   (doto (.getContext canvas "2d")
     (.lineWidth (:width opts 1))
     (.moveTo x1 y1)
     (.lineTo x2 y2)
     (.stroke))))

(defn draw-rect!
  "Draw a rectangle on a canvas"
  [canvas p1 p2 & named_args]
  ; (println p1 p2 opts)
  (let [ctx (.getContext canvas "2d")
        args (apply hash-map named_args)
        color (:color args "black")
        width (- (p2 0) (p1 0))
        height (- (p2 1) (p1 1))]
    (if (:fill args true)
      (doto ctx
        (#(set! (.-fillStyle %) color))
        (.fillRect (p1 0) (p1 1) width height))
      (doto ctx
        (#(set! (.-strokeStyle %) color))
        (.strokeRect (p1 0) (p1 1) width height)
        ))))

(defn draw-circle! [canvas center radius & named_args]
  (let [ctx (.getContext canvas "2d")
        args (apply hash-map named_args)
        color (:color args "black")
        line-width (:line-width args 1)
        x (center 0)
        y (center 1)]
    (doto ctx
      (#(set! (.-fillStyle %) color))
      (#(set! (.-strokeStyle %) color))
      (#(set! (.-lineWidth %) line-width))
      (.beginPath)
      (.arc x y radius 0 (* 3 js/Math.PI))
      (.fill)
      (.stroke))))

(defn canvas-width [canvas]
  (js/parseInt (.-width (.getComputedStyle js/window canvas))))

(defn canvas-height [canvas]
  (js/parseInt (.-height (.getComputedStyle js/window canvas))))

(defn get-canvas-props [width height num-x num-y]
  "Get a map of canvas properties. This map is required in many other functions."
  {:canvas-width  width
   :canvas-height height
   :square-width  (/ width num-x)
   :square-height (/ width num-y)
   :num-x         num-x
   :num-y         num-y})

;;;;;;;;;;; grid squares

(defn get-grid-square-coordinate
  "Returns a single coordinate vector for a grid square."
  [props x y coordinate]
  (let [c #(vector (js/parseInt (* %1 %3)) (js/parseInt (* %2 %4)))]
    (condp = coordinate
      :bottom-left (c x y (:square-width props) (:square-height props))
      :bottom-right (c (inc x) y (:square-width props) (:square-height props))
      :top-left (c x (inc y) (:square-width props) (:square-height props))
      :top-right (c (inc x) (inc y) (:square-width props) (:square-height props))
      :center (c (+ x 0.5) (+ y 0.5) (:square-width props) (:square-height props))
      )))

(defn get-grid-square
  "Returns a map with many data points for a grid square."
  [props x y]
  {:x            x
   :y            y
   :width        (:square-width props)
   :height       (:square-height props)
   :bottom-left  (get-grid-square-coordinate props x y :bottom-left)
   :bottom-right (get-grid-square-coordinate props x y :bottom-right)
   :top-left     (get-grid-square-coordinate props x y :top-left)
   :top-right    (get-grid-square-coordinate props x y :top-right)
   :center       (get-grid-square-coordinate props x y :center)})

(defn get-grid-squares
  "Get a vector of vectors containing grid square maps."
  [props]
  (mapv (fn [x] (mapv (fn [y] (get-grid-square props x y)) (range (:num-y props)))) (range (:num-x props))))

(defn draw-grid-square!
  [canvas props square]
  ; (println (:bottom-left square) (:top-right square))
  (let [alt (even? (+ (:x square) (:y square)))]
    (draw-rect! canvas
                (world-to-canvas-coords (:bottom-left square) (:canvas-height props))
                (world-to-canvas-coords (:top-right square) (:canvas-height props))
                :color (if alt "white" "#CCCCCC")
                :fill true)))

;;;;;;;;;;;;;;;;;; ents, drawing

(defn rand-pieces []
  [{:type  "Rk"
    :owner 0
    :pos   [3 4]}
   {:type  "Bs"
    :owner 1
    :pos   [5 5]}
   {:type  "Kn"
    :owner 1
    :pos   [0 1]}])

(defn ent-color [ent]
  (if (= 0 (:owner ent)) "red" "blue"))

(defn draw-ent! [canvas props ent]
  (let [x (get-in ent [:pos 0])
        y (get-in ent [:pos 1])
        square (get-grid-square props x y)]
    (draw-circle! canvas (:center square) (* 0.4 (:square-width props)) :color (ent-color ent))))

(defn draw! [canvas ents]
  (let [props (get-canvas-props (canvas-width canvas) (canvas-height canvas) grid-num grid-num)]
    (run! #(draw-grid-square! canvas props %) (flatten (get-grid-squares props)))
    (run! #(draw-ent! canvas props %) ents)))

; for now just re draw every so often
(.setInterval js/window
              (fn [] (if (nil? @*canvas*) nil
                                          (draw! @*canvas* (rand-pieces))))
              draw-frequency)

(defn get-game-state! []
  ())

; for now, just poll the server for new game states every so often
(.setInterval js/window
              (fn [] (if (nil? @*canvas*) nil
                                          (draw! @*canvas* (rand-pieces))))
              draw-frequency)

(defn some-tests []
  (let [c @*canvas*]
    (draw-rect! c [0 0] [50 50] {:color "#EFEFEF"})
    (draw-rect! c [0 450] [50 500])
    (draw-rect! c (world-to-canvas-coords [0 0] 500) (world-to-canvas-coords [50 50] 500) {:color "#EEE"})
    (draw-circle! c [200 200] 10 :color "blue")))

(.setTimeout js/window some-tests 1000)

;;;;;;;;;;;;;;;;;;;;;; reagent, components

(defn manual-re-render []
  (swap! app-state update :counter inc))

(def -console-vec (atom []))

(defn log "Adds text to on-screen console" [& args]
  (swap! -console-vec conj args))

(defn btn [key text callback]
  [:button {:key key :style {:margin "10px" :display "inline-block"} :on-click callback} text])

(def board-component
  (r/create-class
    {:component-did-mount (fn [idk] (reset! *canvas* (.getElementById js/document "canvas")))
     :reagent-render      (fn [{:keys [x-count y-count pieces]}] [:div.board [:canvas#canvas {:width 500 :height 500}]])}))

(defn console-component [items-vec]
  "on page console for debugging"
  [:div.console
   (map (fn [key val] [:p {:key key} (str key ": " val)]) (reverse (range (count items-vec))) (reverse items-vec))])

(defn js-event->value [e]
  (.-value (.-target e)))

; returns a fn that sets a key of app-state according to event.target.value.
; suitable for input elements and more
(defn get-event-state-updater [key]
  (fn [e] (set-state key (js-event->value e))))

(defn input-props [state-key]
  {:value (get-state state-key) :on-change (get-event-state-updater state-key)})

(defn join-game "Connects to a game via ID" [game-id]
  ())

(defn choose-game-component []
  [:div.choose-game
   [:input (input-props :desired-game-id)]
   [btn 0 "Submit Game ID" #(set-state :game-id (get-state :desired-game-id))]])

(defn app-component []
  (log "app render")
  [:div.app-inner
   [:h1 "Game"]
   [:div.state (str @app-state)]
   [:div.btns
    [btn 1 "render" #(manual-re-render)]
    [btn 2 "test log" #(do (log "test") (manual-re-render))]]
   [choose-game-component]
   [console-component @-console-vec]
   [:br]
   [board-component {:x-count grid-num :y-count grid-num :pieces []}]])
