(ns kite.context)

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.category :refer :all]
  '[kite.monad :refer :all])

(load "context/config")
(load "context/common")
(load "context/threadpool")
(load "context/forkjoin")
(load "context/execution")

;; eof
