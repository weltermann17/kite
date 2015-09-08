(ns kite-test
  (:refer-clojure :exclude [await future promise]))

(require '[expectations :refer :all]
         '[kite.control :refer :all]
         '[kite.category :refer :all]
         '[kite.context :refer :all]
         '[kite.execution :refer :all]
         '[kite.monad :refer :all]
         '[kite.concurrent :refer :all]
         '[kite.aio :refer :all])

(load "kite_test/execution")
(load "kite_test/sequential")
(load "kite_test/maybe")
(load "kite_test/either")
(load "kite_test/reader")
(load "kite_test/result")
(load "kite_test/future")
(load "kite_test/aio")

;; eof
