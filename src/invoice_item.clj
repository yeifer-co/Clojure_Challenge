(ns invoice-item)
(require '[clojure.edn :as edn])

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))

;^; Problem 1 Solution

; Load invoice to play around with it
(def invoice (edn/read-string (slurp "invoice.edn")))

; Condition 1: At least have one item that has :iva 19%
(defn condition-1 [item]
  (some #(= 19 (:tax/rate %)) (:taxable/taxes item)))

; Condition 2: At least one item has retention :ret_fuente 1%
(defn condition-2 [item]
  (some #(= 1 (:retention/rate %)) (:retentionable/retentions item)))

(defn check-conditions [item]
  ; Every item must satisfy EXACTLY one of the above two conditions.
  (if (condition-1 item)
    (not (condition-2 item))
    (condition-2 item)))

; Filter invoice items by conditions
(defn filter-by-conditions [invoice]
  ; Use ->> to thread the invoice items through the filter
  (->> invoice ; thread operator allows to pass the result of the previous expression as the last argument of the next expression
       :invoice/items
       (filter check-conditions)))

(def result (filter-by-conditions invoice))
