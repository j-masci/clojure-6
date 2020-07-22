(ns utils.core
  "Generic utils")

(defmacro with-try-catch [body exception-handler]
  (try body (catch Exception e (exception-handler e))))

(defn try-catch-print
  "Wraps f around a try-catch and prints the message. Optionally supply
  the caught exception handler."
  ([f] (try-catch-print f #(println "Exception Caught: " (.getMessage %))))
  ([f handler]
   (try
     (f)
     (catch Exception e (handler e)))))