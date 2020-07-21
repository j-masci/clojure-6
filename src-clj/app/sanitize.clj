(ns sanitize
  "User input sanitation functions"
  (:require [hiccup.util]))

;(defn scalar? [thing]
;  (or (string? thing) (integer? thing) (float? thing) (boolean? thing)))

(defn esc-html [str]
  (when (string? str) (hiccup.util/escape-html str)))