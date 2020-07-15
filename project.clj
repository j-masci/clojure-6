(defproject clojure-6 "1.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 ; [org.clojure/clojurescript "1.10.514"]
                 ; [org.clojure/clojurescript "1.10.514"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [com.h2database/h2 "1.4.200"]
                 [reagent "0.10.0"]
                 [binaryage/oops "0.7.0"]
                 [com.bhauman/figwheel-main "0.2.9"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]
                 ]
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-ring "0.12.5"]
            [lein-ancient "0.6.15"]]
  :source-paths ["src-clj" "src-cljc"]
  :resource-paths ["resources"]
  :cljsbuild {:builds [{
                        :source-paths ["src-cljs"]
                        :compiler     {
                                       :output-to     "resources/public/js/app.js"
                                       :output-dir     "resources/public/js/out"
                                       :optimizations :whitespace
                                       ; :source-map false
                                       ; :source-map "resources/public/js/app.js.map"
                                       :pretty-print  true}}]}

  ;:profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.9"]
  ;                                [com.bhauman/rebel-readline-cljs "0.1.4"]]}}

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "--build" "dev" "--repl"]}

  :ring {:handler routes/app}
  :main _core)
