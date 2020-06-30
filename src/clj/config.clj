(ns config)

(def env (read-string
              (try
                (slurp "env.edn")
                (catch Exception e
                  (println "You must create a env.edn file in the root directory. See env-example.edn." "(" e ")")))))