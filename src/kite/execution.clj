(ns kite.execution
  (:refer-clojure :exclude [await future promise]))

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.control :refer :all]
  '[kite.context :refer :all]
  '[kite.category :refer :all]
  '[kite.monad :refer :all])

(load "execution/implicit")
(load "execution/execute")
(load "execution/scheduler")
(load "execution/singlethread")
(load "execution/threadpool")
(load "execution/forkjoin")
(load "execution/context")
(load "execution/promise")
(load "execution/future")

;; eof
