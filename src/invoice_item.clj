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

(defn filter-by-conditions [invoice]
  (->> invoice
       :invoice/items
       (filter (fn [item]
                 (let [taxes       (:taxable/taxes item)
                       retentions  (:retentionable/retentions item)]
                   (if (some #(= 19 (:tax/rate %)) taxes)
                     (not-any? #(= 1 (:retention/rate %)) retentions)
                     (some #(= 1 (:retention/rate %)) retentions)))))))

(def result (filter-by-conditions invoice))
