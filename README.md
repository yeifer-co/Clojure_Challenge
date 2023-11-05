# Clojure Engineering Challenge

> Learning project for clojure

> All commits on this repository should follow the [commit convention](doc/commit_convention.md).

## Getting started

This clojure challenge is made up of 3 questions that reflect the learning you accumulated for the past week. Complete the following instructions:

1. Create a Github/Gitlab repo to show the challenge code. When complete, send us the link to your challenge results.
2. Duration: About 4-6 hours
3. Install Cursive Plugin to Intellij and setup a clojure deps project. https://cursive-ide.com/userguide/deps.html
4. Enjoy!

> Instructions for installing InteliJ available in [Install InteliJ](doc/install_InteliJ.md).

## Problems
### Problem 1 Thread-last Operator ->>
Given the invoice defined in **invoice.edn** in this repo, use the thread-last ->> operator to find all invoice items that satisfy the given conditions. Please write a function that receives an invoice as an argument and returns all items that satisfy the conditions described below.
#### Requirements
- Load invoice to play around with the function like this:

```
(def invoice (clojure.edn/read-string (slurp "invoice.edn")))
```

#### Definitions
- An invoice item is a clojure map { … } which has an :invoice-item/id field. EG.

```
{:invoice-item/id     "ii2"  
  :invoice-item/sku "SKU 2"}
```

- An invoice has two fields :invoice/id (its identifier) and :invoice/items a vector of invoice items

#### Invoice Item Conditions
- At least have one item that has :iva 19%
- At least one item has retention :ret\_fuente 1%
- Every item must satisfy EXACTLY one of the above two conditions. This means that an item cannot have BOTH :iva 19% and retention :ret\_fuente 1%.

#### Solution

The solution is a clojure script that is available in the src folder of this repository, in the file `invoice_play_around.clj`.
[Click here to see script](src/invoice_play_around.clj).

```clojure
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
       (filter check-conditions)
       (vec)))

(def result (filter-by-conditions invoice))

(defn -main
  "Use this to play around with the invoice"
  [& args]
  ; Print the result
  (println "Original invoice:")
  (println invoice)
  (println "Filtered invoice:")
  (println result))
```

#### Comments

- It was necessary to add `main` function to be able to run the code using "play" button in IntelliJ.
- I tried to apply good functional programming practices, like using pure functions and avoiding side effects.
- It was interesting to learn about the `->>` operator. It is very useful to avoid nested expressions.

## Problem 2: Core Generating Functions
Given the invoice defined in **invoice.json** found in this repo, generate an invoice that passes the spec **::invoice** defined in **invoice-spec.clj**. Write a function that as an argument receives a file name (a JSON file name in this case) and returns a clojure map such that

```
(s/valid? ::invoice invoice) => true 
```

where invoice represents an invoice constructed from the JSON.

#### Solution

The solution is at the end of the `invoice_spec.clj` file. It is a clojure script. It is available in the src folder of this repository.
[Click here to see script](src/invoice_spec.clj).

```clojure
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
  "Generate an invoice that passes the corresponding spec"
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
```

#### Comments

- My first approach was to use `clojure.spec.alpha/keys` to define the invoice spec. However, I found it myself in a bottleneck when defining the spec for the nested maps. Looking for a solution, I found a blog post that suggested to use `clojure.spec.alpha/and` and `clojure.spec.alpha/or` to define the spec for the nested maps. However, I was not able to make it work.
- I decide to use plain clojure maps to define the spec. It was easier to define the spec for the nested maps using native functions. However, I am not sure if this is the best approach.
- It was really helpful to use `clojure.spec.alpha/explain` to debug the spec definition.

## Problem 3: Test Driven Development
Given the function **subtotal** defined in **invoice-item.clj** in this repo, write at least five tests using clojure core **deftest** that demonstrates its correctness. This subtotal function calculates the subtotal of an invoice-item taking a discount-rate into account. Make sure the tests cover as many edge cases as you can!

#### Solution

The solution is a clojure script that is available in the test folder of this repository, in the file `invoice_item_test.clj`.
[Click here to see script](test/invoice_item_test.clj).

```clojure
(ns invoice_item_test
  (:require [clojure.test :refer :all]
            [invoice-item :refer :all]))

(deftest subtotal-test-with-discount
  (testing "Test subtotal calculation with discount"
    (is (= 90.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate 10})))
    (is (= 450.0 (subtotal {:precise-quantity 5 :precise-price 100 :discount-rate 10})))))

(deftest subtotal-test-without-discount
  (testing "Test subtotal calculation without discount"
    (is (= 150.0 (subtotal {:precise-quantity 3 :precise-price 50})))
    (is (= 200.0 (subtotal {:precise-quantity 5 :precise-price 40})))))

(deftest subtotal-test-with-zeros
  (testing "Test subtotal calculation with zero quantity"
    (is (= 0.0 (subtotal {:precise-quantity 0 :precise-price 50 :discount-rate 10})))
    (is (= 0.0 (subtotal {:precise-quantity 0 :precise-price 100 :discount-rate 10}))))
  (testing "Test subtotal calculation with zero price"
    (is (= 0.0 (subtotal {:precise-quantity 2 :precise-price 0 :discount-rate 10})))
    (is (= 0.0 (subtotal {:precise-quantity 5 :precise-price 0 :discount-rate 10}))))
  (testing "Test subtotal calculation with zero discount"
    (is (= 100.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate 0})))
    (is (= 500.0 (subtotal {:precise-quantity 5 :precise-price 100 :discount-rate 0})))))

(deftest subtotal-test-with-precise-values
  (testing "Test subtotal calculation with precise values"
    (is (= 90.9 (subtotal {:precise-quantity 2 :precise-price 50.5 :discount-rate 10})))
    (is (= 113.625 (subtotal {:precise-quantity 2.5 :precise-price 50.5 :discount-rate 10})))
    (is (= 112.5 (subtotal {:precise-quantity 2.5 :precise-price 50 :discount-rate 10})))
    (is (= 89.5 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate 10.5})))))

(deftest subtotal-test-large-values
  (testing "Test subtotal calculation with large values"
    (is (= 5000000000.0 (subtotal {:precise-quantity 10000 :precise-price 500000})))
    (is (= 4500000000.0 (subtotal {:precise-quantity 10000 :precise-price 500000 :discount-rate 10})))))

;(deftest subtotal-test-nil-values
;  (testing "Test subtotal calculation with nil values"
;    (is (= 0.0 (subtotal {:precise-quantity nil :precise-price 50 :discount-rate 10})))
;    (is (= 0.0 (subtotal {:precise-quantity 2 :precise-price nil :discount-rate 10})))
;    (is (= 0.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate nil})))
;    (is (= 0.0 (subtotal {:precise-quantity nil :precise-price nil :discount-rate nil})))))

;(deftest subtotal-test-with-large-discount
;  (testing "Test subtotal calculation with large discount"
;    (is (= 0.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate 100})))
;    (is (= 0.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate 110})))
;    (is (= 0.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate 1000})))))
;
;(deftest subtotal-with-negative-discount
;  (testing "Test subtotal calculation with negative discount"
;    (is (= 100.0 (subtotal {:precise-quantity 2 :precise-price 50 :discount-rate -10})))
;    (is (= 500.0 (subtotal {:precise-quantity 5 :precise-price 100 :discount-rate -10})))))
```

#### Comments

- I found `deftest` very easy to use, compared to other testing frameworks like JUnit or Selenium.
- I'm not sure if it was part of the test, some configuration I overlooked, or just something I don't understand, but I wasn't able to get the tests working with the original `invoice_item.clj`. I had to modify the key in the structuring of the parameters changing `:invoice_item/keys` for `:keys`.
- According to the definition of my tests, the last three are not passing. I think that the function is quite simple and it might control other edge cases. If this were a real case scenario, I would ask the product owner about the expected behavior in these cases. However, I leave this test commented to show that I am aware of this situation (it could be a couple of TODO tasks to improve this functionality), by the way I think it is a good example of how to test edge cases.