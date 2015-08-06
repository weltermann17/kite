(in-ns 'kite.monad)

(declare maybe nothing)

(defprotocol Maybe)

(defprotocol Nothing)

(defprotocol Just)

(declare just?)

(def just
  (memoize
    (fn [v]
      (reify
        Maybe
        Just

        IDeref
        (deref [_] v)

        Object
        (equals [this o] (test-eq this o Just #(= v @o)))
        (hashCode [_] (hash v))
        (toString [_] (str "Just " v))

        Functor
        (-fmap [_ f] (just (f v)))

        Pure
        (-pure [_ u] (just u))

        Applicative
        (-apply [_ mv] (just? mv (comp just v) nothing))

        Monad
        (-bind [_ f] (f v))

        IMatchLookup
        (val-at [_ k d] (if (= Just k) v d))))))

(def nothing
  (reify
    Maybe
    Nothing

    Object
    (toString [_] "Nothing")
    (equals [this o] (identical? this o))

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

(defn maybe [v]
  (if v (try (just v) (catch Throwable e (fatal?! e) nothing)) nothing))

(defn just? [m f d]
  (matchm [m]
          [{Just v}] (f v)
          [{Nothing _}] d))

;; eof
