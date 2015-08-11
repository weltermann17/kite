(in-ns 'kite.monad)

(defprotocol Maybe)
(defprotocol Just)
(defprotocol Nothing)

(declare just? nothing)

(defn just [v]
  {:pre [(not (nil? v))]}
  (reify
    Maybe
    Just

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (test-eq this o Just #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (<< "Just ~{v}"))

    Functor
    (-fmap [_ f] (just (f v)))

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-apply [_ m] (just? m (comp just v) nothing))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k d] (if (= Just k) v d))))

(def nothing
  (reify
    Maybe
    Nothing

    Object
    (equals [this o] (identical? this o))
    (toString [_] "Nothing")

    IFn
    (invoke [this]
      "To allow 'nothing' to be called as a 0 arity function."
      this)

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

(defmacro maybe [body]
  `(try (just ~body)
        (catch Throwable e# (fatal?! e#) nothing)))

(defn just? [m f d]
  (matchm [m]
          [{Just v}] (f v)
          [{Nothing _}] d))

;; eof
