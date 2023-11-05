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