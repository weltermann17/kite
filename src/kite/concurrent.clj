(ns kite.concurrent
  (:refer-clojure :exclude [await future promise]))

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.control :refer :all]
  '[kite.category :refer :all]
  '[kite.monad :refer :all]
  '[kite.execution :refer :all])

(load "concurrent/future")

;; eof
