(ns app.game
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]))

(enable-console-print!)

(def grid-num 7)

(defonce ^:dynamic *canvas* (atom nil))

(defonce app-state (r/atom {:idk        []
                            :counter    0}))

(defn get-context [canvas]
  (.getContext canvas "2d"))

(defn get-canvas-width [canvas]
  (js/parseInt (.-width (js/window.getComputedStyle canvas))))

(defn get-canvas-height [canvas]
  (js/parseInt (.-height (js/window.getComputedStyle canvas))))

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

; optimize later if needed
(defn get-grid-square [x y square-width square-height]
  (let [coord (fn [_x _y] (vector (js/parseInt (* _x square-width)) (js/parseInt (* _y square-height))))]
    {:x            x
     :y            y
     :width        square-width
     :height       square-height
     :bottom-left  (coord x y)
     :bottom-right (coord (inc x) y)
     :top-left     (coord x (inc y))
     :top-right    (coord (inc x) (inc y))
     :center       (coord (+ x 0.5) (+ y 0.5))}))

(defn get-canvas-props [width height num-x num-y]
  (let [square-width (/ width num-x)
        square-height (/ width num-y)]
    {:canvas-width  width
     :canvas-height height
     :square-width  square-width
     :square-height square-height
     :num-x         num-x
     :num-y         num-y
     :squares       (mapv (fn [x] (mapv (fn [y] (get-grid-square x y square-width square-height)) (range num-y))) (range num-x))}))

(defn get-square-center [props x y]
  (let []))

(defn get-canvas-props-via-canvas [canvas num-x num-y]
  (get-canvas-props (get-canvas-width canvas) (get-canvas-height canvas) num-x num-y))

(defn world-to-canvas-coords [point canvas-height]
  (vector (point 0) (- canvas-height (point 1))))

(defn draw-grid-square!
  [canvas props square]
  (draw-rect! canvas
              (world-to-canvas-coords (:bottom-left square) (:canvas-height props))
              (world-to-canvas-coords (:top-right square) (:canvas-height props))
              :color (if (even? (+ (:x square) (:y square))) "grey" "white")))

(defn draw-grid-squares! [canvas props]
  (run! #(run! (fn [square] (draw-grid-square! canvas props square)) %) (:squares props)))

(defn draw! [canvas]
  (pprint "DRAW asd....")
  (draw-grid-squares! canvas (get-canvas-props-via-canvas canvas grid-num grid-num)))

; re-draw every so often
(js/window.setInterval
  (fn [] (if (nil? @*canvas*) nil
                              (draw! @*canvas*)))
  500)

; (js/window.setTimeout #(draw-rect! @*canvas* [0 0] [50 50] {:color "#EFEFEF"}) 1000)
; (js/window.setTimeout #(draw-rect! @*canvas* [0 450] [50 500]) 1000)
; (js/window.setTimeout #(draw-rect! @*canvas* (world-to-canvas-coords [0 0] 500) (world-to-canvas-coords [50 50] 500) {:color "#EEE"}) 1000)

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
    {:component-did-mount (fn [idk] (reset! *canvas* (js/document.getElementById "canvas")))
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
