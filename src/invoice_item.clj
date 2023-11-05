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

(def invoice (edn/read-string (slurp "invoice.edn")))

(defn condition-1 [item]
  (some #(= 19 (:tax/rate %)) (:taxable/taxes item)))

(defn condition-2 [item]
  (some #(= 1 (:retention/rate %)) (:retentionable/retentions item)))

(defn check-conditions [item]
  (if (condition-1 item)
    (not (condition-2 item))
    (condition-2 item)))

(defn filter-by-conditions [invoice]
  (->> invoice
       :invoice/items
       (filter check-conditions)))

(def result (filter-by-conditions invoice))
