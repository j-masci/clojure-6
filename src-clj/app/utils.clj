(ns utils)

(defn try-catch-print
  "Wraps f around a try-catch and prints the message"
  ([f] (try-catch-print f #(println "Exception Caught: " (.getMessage %))))
  ([f handler]
   (try
     (f)
     (catch Exception e (handler e)))))