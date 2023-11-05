(ns invoice-spec
  (:require
    [clojure.data.json :as json]
    [clojure.spec.alpha :as s])
  (:import
           (java.text SimpleDateFormat)))

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

(s/def :tax/rate double?)
(s/def :tax/category #{:iva})
(s/def ::tax (s/keys :req [:tax/category
                           :tax/rate]))
(s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

(s/def :invoice-item/price double?)
(s/def :invoice-item/quantity double?)
(s/def :invoice-item/sku non-empty-string?)

(s/def ::invoice-item
  (s/keys :req [:invoice-item/price
                :invoice-item/quantity
                :invoice-item/sku
                :invoice-item/taxes]))

(s/def :invoice/issue-date inst?)
(s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

(s/def ::invoice
  (s/keys :req [:invoice/issue-date
                :invoice/customer
                :invoice/items]))

;^; Problem 2 Solution
(defn parse-date
  "Parse a date string into a date object"
  [date-string]
  (let [date-formatter (SimpleDateFormat. "dd/MM/yyyy")]
    (.parse date-formatter date-string)))

(defn read-json-file
  "Read a JSON file and return a map"
  [file-name]
  (json/read-str (slurp file-name) :key-fn keyword))

(defn get-issue-date
  "Get the issue date from the invoice"
  [invoice]
  (parse-date (get-in invoice [:invoice :issue_date])))

(defn get-customer
  "Get the customer from the invoice"
  [invoice]
  (let [customer (get-in invoice [:invoice :customer])]
    {:customer/name  (get-in customer [:company_name])
     :customer/email (get-in customer [:email])}))

(defn get-items
  "Get the items from the invoice"
  [invoice]
  (let [items (get-in invoice [:invoice :items])]
    (map (fn [item]
           {:invoice-item/price    (get-in item [:price])
            :invoice-item/quantity (get-in item [:quantity])
            :invoice-item/sku      (get-in item [:sku])
            :invoice-item/taxes    (vec (map (fn [tax]
                                               {
                                                :tax/category (keyword (clojure.string/lower-case (get-in tax [:tax_category])))
                                                :tax/rate     (double (get-in tax [:tax_rate]))
                                                })
                                             (get-in item [:taxes])))
            })
         items)))

(defn generate-invoice
  "Generate an invoice that passes the spec ::invoice defined in invoice-spec.clj"
  [file-name]
  (let [invoice (read-json-file file-name)]
    {:invoice/issue-date (get-issue-date invoice)
     :invoice/customer  (get-customer invoice)
     :invoice/items     (vec (get-items invoice))}))

(defn -main
  "Use this execute the invoice spec"
  [& args]
  (let [invoice (generate-invoice "invoice.json")]
    (println (s/valid? ::invoice invoice))
    (s/explain ::invoice invoice)))
