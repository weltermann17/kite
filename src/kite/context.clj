(ns kite.context)

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.control :refer :all]
  '[kite.category :refer :all]
  '[kite.monad :refer :all])

;;(load "context/aspect")
(load "context/config")
(load "context/singlethread")
(load "context/threadpool")
(load "context/forkjoin")
(load "context/executor")

;; eof
