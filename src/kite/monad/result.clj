(in-ns 'kite.monad)

(defprotocol Result)

(defprotocol Success
  (-success [_]))

(defprotocol Failure
  (-failure [_]))

(declare failure success)

(defmacro result [& body]
  `(try
     (success (do ~@body))
     (catch Throwable e# (failure e#))))

(defn success [v]
  {:pre [(not (nil? v))
         (not (fatal? v))]}
  (reify
    Result
    Success
    (-success [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (test-eq this o Success #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Success " v))

    Functor
    (-fmap [_ f] (result (f v)))

    Pure
    (-pure [_ u] (success u))

    ;; Applicative
    ;; (-ap [_ m] (match-result m (comp success v) (failure v)))

    Monad
    (-bind [_ f] (try (f v) (catch Throwable e (failure e))))

    ;; MonadPlus

    IMatchLookup
    (val-at [_ k d] (if (= Success k) v d))))

(defn failure [v]
  {:pre [(or (fatal?! v) (nil? v))]}
  (reify
    Result
    Failure
    (-failure [_] v)

    IDeref
    (deref [_]
      "Throws v if it is a Throwable else returns it."
      (if (instance? Throwable v) (throw v) v))

    Object
    (equals [this o] (test-eq this o Failure #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Failure " v))

    Functor
    (-fmap [m _] m)

    Pure
    (-pure [_ u] (success u))

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k d] (if (= Failure k) v d))))

(defn success?
  ([r succ fail]
   "A lot like an 'if': if 'r' is a Success apply 'succ' to it else apply 'fail' to it.
    Note: succ/fail must expect the Result, not the dereferenced internal value."
   (matchm [r]
           [{Success _}] (succ r)
           [{Failure _}] (fail r)))
  ([r]
   "Just testing for instance of Success."
   (satisfies? Success r)))

(defn failure?
  ([r fail succ]
   (success? r succ fail))
  ([r]
   (satisfies? Failure r)))

;; eof
