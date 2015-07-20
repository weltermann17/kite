(ns kite-test)

(require '[expectations :refer :all]
         '[kite :refer :all])

(expect "f1" (f 1))

;; eof
