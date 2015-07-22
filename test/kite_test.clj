(ns kite-test
  (:refer-clojure :exclude [await future promise]))

(require '[expectations :refer :all]
         '[kite :refer :all])

(expect 1 1)

(expect 2 2)

;; eof
