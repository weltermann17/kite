(ns kite.monad)

(import
  [clojure.lang IDeref IFn])

(require
  '[clojure.core.strint :refer [<<]]
  '[clojure.core.match :refer [match matchm]]
  '[clojure.core.match.protocols :refer [IMatchLookup]]
  '[kite.control :refer :all]
  '[kite.category :refer :all])

(load "monad/identity")
(load "monad/function")
(load "monad/sequential")
(load "monad/maybe")
(load "monad/either")
(load "monad/reader")
(load "monad/result")

(comment IDeref IFn)

;; eof
