(ns app.utils.core
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

(defn idk-yet-123
  "Pass in a function f and a function args-filter,
  returns a function that invokes f but with the vector
  of arguments that args-filter returns. args-filter accepts
  and must return a vector of function arguments."
  [f args-filter] (fn [& args] (apply f (args-filter (into [] args)))))