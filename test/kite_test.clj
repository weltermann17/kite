(ns kite-test
  (:refer-clojure :exclude [await future promise]))

(require '[expectations :refer :all]
         '[kite.category :refer :all]
         '[kite :refer :all])

(load "kite_test/sequential")
(load "kite_test/maybe")
(load "kite_test/either")
(load "kite_test/result")
(load "kite_test/future")

;; eof
