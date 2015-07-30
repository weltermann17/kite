(in-ns 'kite)

(defprotocol Result)

(defprotocol Success
  (-success [_]))

(defprotocol Failure
  (-failure [_]))

(declare match-result failure)

(defn success [v]
  {:pre [(fatal?! v)]}
  (reify
    Result
    Success
    (-success [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (equal? this o Success #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Success " v))

    Functor
    (-fmap [_ f] (success (f v)))

    Pure
    (-pure [_ u] (success u))

    ;Applicative
    ;(-ap [_ m] (match-result m (comp success v) (failure v)))

    Monad
    (-bind [_ f] (try (f v) (catch Throwable e (failure e))))

    IMatchLookup
    (val-at [_ k d] (if (= Success k) v d))))

(defn failure [v]
  {:pre [(or (fatal?! v) (nil? v))]}
  (reify
    Result
    Failure
    (-failure [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (equal? this o Failure #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Failure " v))

    Functor
    (-fmap [m _] m)

    Pure
    (-pure [_ u] (success u))

    ;Applicative
    ;(-ap [_ m] no-such-method!)

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k d] (if (= Failure k) v d))))

(defn success? [r succ fail]
  "If 'r' is success apply 'succ' to it else apply 'fail'."
  (matchm [r]
          [{Success _}] (succ r)
          [{Failure _}] (fail r)))

(defmacro result [& body]
  `(try
     (success (do ~@body))
     (catch Throwable e# (failure e#))))

;; eof
