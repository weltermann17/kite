(in-ns 'kite)

(defprotocol Try)

(defprotocol Success
  (-success [_]))

(defprotocol Failure
  (-failure [_]))

(declare ->try failure)

(defn success [v]
  (reify
    Try
    Success
    (-success [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [_ o] (and (satisfies? Success o) (= v (-success o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Success " v))

    Functor
    (-fmap [_ f] (success (f v)))

    Pure
    (-pure [_ u] (success u))

    Monad
    (-bind [_ f] (try (f v) (catch Throwable e (fatal?! e) (failure e))))

    IMatchLookup
    (val-at [_ k d] (case k ::success v d))))

(defn failure [v]
  (reify
    Try
    Failure
    (-failure [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [_ o] (and (satisfies? Failure o) (= v (-failure o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Failure " v))

    Functor
    (-fmap [m _] m)

    Pure
    (-pure [_ u] (success u))

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k d] (case k ::failure v d))))

(defn match-try [t succ fail]
  (matchm [t]
          [{::failure _}] (fail t)
          [{::success _}] (succ t)))

(defmacro ->try [& body]
  `(try
     (success (do ~@body))
     (catch Throwable e# (fatal?! e#) (failure e#))))

(defn try-fn [f] (->try (f)))

;; eof
