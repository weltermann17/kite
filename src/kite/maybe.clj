(in-ns 'kite)

(import
  [clojure.lang IFn])

(declare maybe nothing)

(defprotocol Maybe)

(defprotocol Nothing)

(defprotocol Just)

(defn just [v]
  (reify
    Maybe
    Just

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (equal? this o Just #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Just " v))

    Functor
    (-fmap [_ f] (just (f v)))

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-apply [_ mv] (maybe nothing (comp just v) mv))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k d] (if (= Just k) v d))))

(def nothing
  (reify
    Maybe
    Nothing

    Object
    (toString [_] "Nothing")
    (equals [this o] (identical? this o))

    IFn
    (invoke [_]
      "To allow nothing to be called as a 0 arity function."
      nothing)

    Functor
    (-fmap [_ _] nothing)

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-apply [_ _] nothing)

    Monad
    (-bind [_ _] nothing)

    IMatchLookup
    (val-at [_ k d] (if (= Nothing k) nil d))))

(defn maybe
  ([v]
   (if v (try (just v) (catch Throwable e (fatal?! e) nothing)) nothing))
  ([d f m]
   (matchm [m]
           [{Just v}] (f v)
           [{Nothing _}] d)))

;; eof
