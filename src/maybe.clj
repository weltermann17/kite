(in-ns 'kite)

(import
  [clojure.lang IFn IDeref])

(require
  '[clojure.core.match :refer [matchm]]
  '[clojure.core.match.protocols :refer [IMatchLookup]])

(declare maybe nothing)

(defprotocol Maybe)
(defprotocol Nothing)
(defprotocol Just
  (value [_]))

(defn just [v]
  (reify
    Maybe
    Just
    (value [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [_ o] (and (satisfies? Just o) (= v (value o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Just " v))

    Functor
    (-fmap [_ f] (just (f v)))

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-ap [_ m] (maybe nothing (comp just v) m))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k d] (case k ::just v d))))

(def nothing
  (reify
    Maybe
    Nothing

    Object
    (toString [_] "Nothing")
    (equals [this o] (identical? this o))

    IFn
    (invoke [_]
      "To allow nothing to be called as a 0 arity function." nothing)

    Functor
    (-fmap [_ _] nothing)

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-ap [_ _] nothing)

    Monad
    (-bind [_ _] nothing)

    IMatchLookup
    (val-at [_ k d] (case k ::nothing nil d))))

(defn maybe
  ([v]
   (if v (just v) nothing))
  ([d f m]
   (matchm [m]
           [{::just v}] (f v)
           [{::nothing _}] d)))

;; eof
