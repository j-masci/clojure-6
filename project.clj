(defproject clojure-6 "1.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ; [org.clojure/clojurescript "1.10.514"]
                 [org.clojure/clojurescript "1.10.514"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.6.0"]
                 [com.h2database/h2 "1.4.193"]
                 [reagent "0.10.0"]]
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-ring "0.12.5"]]
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["config", "resources"]
  :cljsbuild {:builds [{
                        :source-paths ["src/cljs"]
                        :compiler     {
                                       :output-to     "resources/public/js/main.js"
                                       :output-dir     "resources/public/js/out"
                                       :optimizations :whitespace
                                       ; :source-map false
                                       ; :source-map "resources/public/js/main.js.map"
                                       :pretty-print  true}}]}
  :ring {:handler routes/app}
  :main _core)
