;^; Problem 2 Solution
(ns invoice
  (:require [clojure.spec.alpha :as s]
            [clojure.data.json :as json]
            [invoice-spec :as spec]))

; Solution
(def all-records (json/read-str (slurp "path/to/file.json") :key-fn keyword))

(print all-records)