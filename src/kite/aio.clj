(ns kite.aio
  (:refer-clojure :exclude [await future promise]))

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.control :refer :all]
  '[kite.context :refer :all]
  '[kite.category :refer :all]
  '[kite.monad :refer :all]
  '[kite.execution :refer :all]
  '[kite.concurrent :refer :all])

(load "aio/buffer")
(load "aio/group")
(load "aio/socket")
(load "aio/server")
(load "aio/context")

;; eof