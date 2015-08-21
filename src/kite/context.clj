(ns kite.context)

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.control :refer :all]
  '[kite.category :refer :all]
  '[kite.monad :refer :all])

(load "context/implicit")
(load "context/access")
(load "context/config")

;; eof
