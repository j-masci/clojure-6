(defproject clojure-6 "1.0"

  :dependencies [[org.clojure/clojurescript "1.10.773"]
                 [org.clojure/clojure "1.10.1"]
                 [compojure "1.6.1"]
                 [org.clojure/data.json "1.0.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [com.h2database/h2 "1.4.200"]

                 ; cljs
                 [reagent "0.10.0"]
                 [binaryage/oops "0.7.0"]

                 [cljs-http "0.1.46"]]

  ; cursive IDE seems to not recognize dependencies among profiles,
  ; so i'm going to put as much as possible in the dependencies above
  ; and only what's necessary to avoid conflicts below.
  :profiles {:client {:dependencies [[com.bhauman/figwheel-main "0.2.10"]
                                     [com.bhauman/rebel-readline-cljs "0.1.4"]]}

             :server {:dependencies []}}

  :plugins [[lein-ring "0.12.5"]
            [lein-ancient "0.6.15"]]

  :source-paths [
                 "src-clj"
                 "src-cljc"
                 ]

  :resource-paths ["resources"]

  :aliases {"fig"       ["with-profile" "client" "trampoline" "run" "-m" "figwheel.main"]
            ; use this one (port will be default of 9500):
            "fig:build" ["with-profile" "client" "trampoline" "run" "-m" "figwheel.main" "--build" "dev" "--repl"]
            ; the server serves resources including css/js/index.html and has api endpoints
            "server"    ["with-profile" "server" "ring" "server" 9501]}

  :ring {:handler app.routes/app}
  :main app._core)
