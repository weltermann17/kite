(ns kite
  (:refer-clojure :exclude [await future promise]))

(import
  [clojure.lang IDeref])

(require
  '[clojure.core.strint :refer [<<]]
  '[clojure.core.match :refer [matchm]]
  '[clojure.core.match.protocols :refer [IMatchLookup]]
  '[kite.control :refer :all]
  '[kite.category :refer :all])

(load "kite/identity")
(load "kite/maybe")
(load "kite/either")
(load "kite/sequential")
(load "kite/reader")
(load "kite/result")
(load "kite/future")

(comment IDeref)

;; eof
