(in-ns 'kite)

(import
  [clojure.lang IDeref]
  [java.util.concurrent Phaser TimeoutException TimeUnit])

(require
  '[clojure.core.strint :refer [<<]])

;; protocols

(defprotocol Promise
  (complete [_ v])
  (->future [_]))

(defprotocol Future
  (await [_ milliseconds] "Should be used for testing only.")
  (on-complete [_ f]))

;; mocks

(defn- execute ([_] nil) ([_ _] nil))
(defn- execute-all [_ _] nil)

;; types

(declare failed immediate mk-future try-future)

(defn- !illegal-state [^String s]
  (throw (IllegalStateException. s)))

(defn ^Promise promise []
  (let [value (volatile! ::incomplete)
        callbacks (volatile! [])
        future (mk-future value callbacks)]
    (reify
      Promise
      (complete [_ v]
        (if (= @value ::incomplete)
          (do (vreset! value v) (execute-all @callbacks v))
          (!illegal-state (<< "A promise cannot be completed more than once, value = ~{value}, value not accepted = ~{v}"))))
      (->future [_] future)

      IDeref
      (deref [_] @value)

      Object
      (equals [_ o] (and (satisfies? Promise o) (= value (deref o))))
      (hashCode [_] (hash value))
      (toString [_] (str "Promise " value)))))

(defn- ^Future mk-future [value callbacks]
  (reify
    Future
    (await [m milliseconds]
      (let [phaser (Phaser. 1)]
        (on-complete m (fn [_] (.arriveAndDeregister phaser)))
        (try
          (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS) m
          (catch TimeoutException _
            (failed (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
          (catch Exception e
            (failed e)))))
    (on-complete [_ f]
      (let [v @value]
        (if (= v ::incomplete)
          (vswap! callbacks conj f)
          (execute f v))))

    IDeref
    (deref [_] @value)

    Object
    (equals [_ o] (and (satisfies? Future o) (= value (deref o))))
    (hashCode [_] (hash value))
    (toString [_] (str "Future " value))

    Functor
    (-fmap [_ f] f)

    Pure
    (-pure [_ u] (immediate u))

    Applicative
    (-ap [_ m] m)

    Monad
    (-bind [_ f] (try-future f))))

;; fn

(defn ^Future future-fn [f]
  (let [p (promise)]
    (execute (fn [] (complete p (try-fn f))))
    (->future p)))

(defn ^Future immediate [v]
  (let [p (promise)]
    (complete p (success v))
    (->future p)))

(defn ^Future failed [v]
  (let [p (promise)]
    (complete p (failure v))
    (->future p)))

;; macros

(defmacro ^Future future [& body]
  `(future-fn (fn [] ~@body)))

(defmacro ^:private ^Future try-future
  [& body]
  `(try
     ~@body
     (catch Exception t#
       (failed t#))))

;; eof
