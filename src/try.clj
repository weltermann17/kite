(in-ns 'kite)

(import
  [clojure.lang IDeref])

(require
  '[clojure.core.match.protocols :refer [IMatchLookup]])

(defprotocol Try)
(defprotocol Success
  (-success [_]))
(defprotocol Failure
  (-failure [_]))

(declare ->try failure)

(defn fatal? [^Throwable e]
  (some #(instance? % e) [InterruptedException
                          LinkageError
                          ThreadDeath
                          VirtualMachineError]))

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
    (-fmap [_ f] (->try (f v)))

    Pure
    (-pure [_ u] (success u))

    Monad
    (-bind [_ f] (try (f v) (catch Throwable e (when (fatal? e) (throw e))
                                               (failure e))))

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        ::success v
        not-found))))

(defn failure [v]
  (reify
    Either
    Failure
    (-failure [_] v)

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
    (val-at [_ k not-found]
      (case k
        ::failure v
        not-found))))

(defmacro ->try [& body]
  `(try
     (success (do ~@body))
     (catch Throwable e# (when (fatal? e#) (throw e#))
                         (failure e#))))

(defn match-try [f g t]
  (match [t]
         [{::failure v}] (f v)
         [{::success v}] (g v)))

(defn try-fn [f] (->try (f)))

(comment match-try try-fn)

;; eof
