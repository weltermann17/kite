(ns kite.aio
  (:refer-clojure :exclude [await future promise]))

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.control :refer :all]
  '[kite.context :refer :all]
  '[kite.category :refer :all]
  '[kite.monad :refer :all]
  '[kite.execution :refer :all]
  '[kite.string :refer :all])

(load "aio/bufferpool")
(load "aio/group")
(load "aio/socket")
(load "aio/clientpool")
(load "aio/client")
(load "aio/server")
(load "aio/context")

;; eof
