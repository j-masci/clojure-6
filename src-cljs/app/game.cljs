(ns app.game
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]))

(enable-console-print!)

(def grid-num 12)

(def draw-frequency 2000)

(defonce ^:dynamic *canvas* (atom nil))

(defonce app-state (r/atom {:idk        []
                            :counter    0}))

(defn get-context [canvas]
  (.getContext canvas "2d"))

(defn world-to-canvas-coords [point canvas-height]
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
  [canvas p1 p2 & opts]
  ; (println p1 p2 opts)
  (let [_opts (apply hash-map opts)
        color (:color _opts "#000000")
        ctx (get-context canvas)
        width (- (p2 0) (p1 0))
        height (- (p2 1) (p1 1))]
    (if (:fill _opts true)
      (doto ctx
        (#(set! (.-fillStyle %) color))
        (.fillRect (p1 0) (p1 1) width height))
      (doto ctx
        (#(set! (.-strokeStyle %) color))
        (.strokeRect (p1 0) (p1 1) width height)
        ))))

(defn get-canvas-width [canvas]
  (js/parseInt (.-width (.getComputedStyle js/window canvas))))

(defn get-canvas-height [canvas]
  (js/parseInt (.-height (.getComputedStyle js/window canvas))))

(defn get-canvas-props [width height num-x num-y]
  "Get a map of canvas properties. This map is required in many other functions."
  {:canvas-width  width
   :canvas-height height
   :square-width  (/ width num-x)
   :square-height (/ width num-y)
   :num-x         num-x
   :num-y         num-y})

(defn get-current-canvas-props []
  (get-canvas-props (get-canvas-width @*canvas*) (get-canvas-height @*canvas*) grid-num grid-num))

(defn -compute-coord
  [x y width height]
  (vector (js/parseInt (* x width)) (js/parseInt (* y height))))

(defn get-grid-square-coordinate
  "Returns a single coordinate vector for a grid square."
  [props x y coordinate]
  (condp = coordinate
    :bottom-left  (-compute-coord x y (:square-width props) (:square-height props))
    :bottom-right (-compute-coord (inc x) y (:square-width props) (:square-height props))
    :top-left     (-compute-coord x (inc y) (:square-width props) (:square-height props))
    :top-right    (-compute-coord (inc x) (inc y) (:square-width props) (:square-height props))
    :center       (-compute-coord (+ x 0.5) (+ y 0.5) (:square-width props) (:square-height props))
    ))

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

(defn draw-grid-squares! [canvas props]
  (run! #(draw-grid-square! canvas props %) (flatten (get-grid-squares props))))

(defn draw! [canvas]
  ; (println "Draw...")
  ; (println (get-grid-squares (get-current-canvas-props)))
  ; (println (get-current-canvas-props))
  (draw-grid-squares! canvas (get-current-canvas-props)))

; re-draw every so often
(.setInterval js/window
  (fn [] (if (nil? @*canvas*) nil
                              (draw! @*canvas*)))
              draw-frequency)

 (js/window.setTimeout #(draw-rect! @*canvas* [0 0] [50 50] {:color "#EFEFEF"}) 1000)
 (js/window.setTimeout #(draw-rect! @*canvas* [0 450] [50 500]) 1000)
 (js/window.setTimeout #(draw-rect! @*canvas* (world-to-canvas-coords [0 0] 500) (world-to-canvas-coords [50 50] 500) {:color "#EEE"}) 1000)

; for now, simply re-draw every so often.
; (js/window.setTimeout #(pprint (get-canvas-props-via-canvas @*canvas* 2 2)) 1000)
; (js/window.setTimeout #(pprint (get-canvas-props-via-canvas @*canvas* 8 8)) 2000)

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

(defn manual-re-render []
  (swap! app-state update :counter inc))

(def -console-vec (atom []))

(defn log "Adds text to on-screen console" [& args]
  (swap! -console-vec conj args))

(defn btn [text callback]
  [:button {:style {:margin "10px" :display "inline-block"} :on-click callback} text])

(def board-component
  (r/create-class
    {:component-did-mount (fn [idk] (reset! *canvas* (.getElementById js/document "canvas")))
     :reagent-render      (fn [{:keys [x-count y-count pieces]}] [:div.board [:canvas#canvas {:width 500 :height 500}]])}))

(defn console-component [items-vec]
  "on page console for debugging"
  (js/console.log items-vec)
  [:div.console
   (let [i (map vector (range (count items-vec)) items-vec)]
     (map #(vector :p {:key i} (str (% 0) ": " (str (% 1)))) (reverse i)))])

(defn app-component []
  (log "app render")
  [:div.app-inner
   [:h1 "Game"]
   [:div.state (str @app-state)]
   [:div.btns
    [btn "render" #(manual-re-render)]
    [btn "test log" #((log "test") (manual-re-render))]]
   [console-component @-console-vec]
   [:br]
   [board-component {:x-count grid-num :y-count grid-num :pieces []}]])
