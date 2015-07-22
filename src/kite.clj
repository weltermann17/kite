(ns kite
  (:refer-clojure :exclude [await future promise]))

(load "impl/protocols")
(load "impl/hierarchy")
(load "impl/functor")
(load "impl/applicative")
(load "impl/monad")
(load "impl/monoid")
(load "identity")
(load "maybe")
(load "either")
(load "sequential")
(load "reader")
(load "try")
(load "future")

;; eof
