(ns game
  (:require [reagent.core :as r]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]))

(def *canvas* (atom nil))

(def app-state (r/atom {:idk        []
                        :counter    0
                        :board-size 7}))

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
  [canvas x1 y1 x2 y2 & opts]
  (let [_opts (apply hash-map opts)
        color (:color _opts "#000000")
        ctx (get-context canvas)]
    (if (:fill opts)
      (doto ctx
        (#(set! (.-fillStyle %) color))
        (.fillRect x1 y1 x2 y2))
      (doto ctx
        (#(set! (.-strokeStyle %) color))
        (.strokeRect x1 y1 x2 y2)))))

(defn gen-canvas-props [canvas num-x num-y]
  {:width  (get-canvas-width canvas)
   :height (get-canvas-height canvas)
   :num-x  num-x
   :num-y  num-y})

(defn get-squares-width [props]
  (/ (:width props) (:num-x props)))

(defn get-squares-height [props]
  (/ (:width props) (:num-x props)))

; optimize later if needed
(defn get-grid-square [props x y]
  (let [width (get-squares-width props)
        height (get-squares-height props)
        coord (fn [_x _y] (vector (int (* _x width)) (int (* _y height))))]
    {:x            x
     :y            y
     :width        width
     :height       height
     :bottom-left  (coord x y)
     :bottom-right (coord (inc x) y)
     :top-left     (coord x (inc y))
     :top-right    (coord (inc x) (inc y))
     :center       (coord (+ x 0.5) (+ y 0.5))}))

(defn get-all-board-squares [props]
  (for [x (range (:num-x props)) y (range (:num-y props))]
    (get-grid-square props x y)))

(defn draw-board-squares! [canvas num-x num-y]
  (let [props (gen-canvas-props canvas num-x num-y)
        squares (get-all-board-squares props)]
    (run! (fn [square] (draw-rect! canvas
                                   ((:bottom-left square) 0)
                                   ((:bottom-left square) 1)
                                   ((:top-right square) 0)
                                   ((:top-right square) 1)
                                   {:color (if (even? (+ (:x square) (:y square))) "#efefef" "#ffffff")}))
          squares)))

(defn draw! [canvas app-state]
  (doto canvas
    (draw-board-squares! 10 10)))

; for now, simply re-draw every so often.
(js/window.setInterval
  (fn [] (if (nil? @*canvas*) nil
                              (draw! @*canvas* @app-state)))
  200)

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

(defn board-square-bottom-left-pct [count total]
  (if (= 0 count) 0 (* 100 (/ count total))))

(println (get-all-board-squares {:width 500 :height 500 :num-x 10 :num-y 10}))

(def board-component
  (r/create-class
    {:component-did-mount (fn [idk] (reset! *canvas* (js/document.getElementById "canvas")))
     :reagent-render      (fn [{:keys [x-count y-count pieces]}] [:div.board [:canvas#canvas {:width 800 :height 600}]])}))

;(defn
;  ^{:component-did-mount (fn [idk] (js/console.log "did mount..." idk) (reset! *canvas* (js/document.getElementById "canvas")))}
;  board-component [{:keys [x-count y-count pieces]}]
;  [:div.board [:canvas#canvas {:width 800 :height 600}]])

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
    [btn "test log" #((log "test") (manual-re-render))]]
   [console-component @-console-vec]
   [:br]
   [board-component {:x-count (:board-size @app-state) :y-count (:board-size @app-state) :pieces []}]])
