;^; Problem 1 Solution
(ns invoice_play_around
  (:require [clojure.edn :as edn]))

; Load invoice to play around with it
(def invoice (edn/read-string (slurp "invoice.edn")))

; Condition 1: At least have one item that has :iva 19%
(defn condition-1?
  "At least have one item that has :iva 19%"
  [item]
  (some #(= 19 (:tax/rate %)) (:taxable/taxes item)))

; Condition 2: At least one item has retention :ret_fuente 1%
(defn condition-2?
  "At least one item has retention :ret_fuente 1%"
  [item]
  (some #(= 1 (:retention/rate %)) (:retentionable/retentions item)))
(def not-condition-2?
  "No item has retention :ret_fuente 1%"
  (complement condition-2?))

(defn check-conditions
  "Check if an item satisfies the conditions"
  [item]
  ; Every item must satisfy EXACTLY one of the above two conditions.
  (if (condition-1? item)
    (not-condition-2? item)
    (condition-2? item)))

; Filter invoice items by conditions
(defn filter-by-conditions
  "Filter invoice items by conditions"
  [invoice]
  ; Use ->> to thread the invoice items through the filter
  (->> invoice ; thread operator allows to pass the result of the previous expression as the last argument of the next expression
       :invoice/items
       (filter check-conditions)))

(def result (filter-by-conditions invoice))

; Print the result
(println result)