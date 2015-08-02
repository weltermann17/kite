(in-ns 'kite.concurrent)

(import
  [clojure.lang IDeref]
  [java.util.concurrent Phaser TimeoutException TimeUnit])


(require
  '[kite.control :refer :all]
  '[kite.monad :refer :all])

;; protocols

(defprotocol Future
  "Future is a Monad."
  (^Result await [_ milliseconds]
    "Should be used for testing only. Milliseconds must be > 0.")
  (on-complete [_ f]))

(defprotocol Promise
  (complete [_ v] "Calls illegal-state! if called more than once.")
  (^Future ->future [_]))

;; types

(declare mk-future)

(defn promise []
  (let [value (volatile! ::incomplete)
        callbacks (volatile! [])
        future (mk-future value callbacks)]
    (reify
      Promise
      (complete [_ v]
        (if (= @value ::incomplete)
          (do (vreset! value v) (execute-all-t @callbacks v))
          (illegal-state! (<< "A promise cannot be completed more than once, value = ~{@value}, value not accepted = ~{v}"))))
      (->future [_] future)

      IDeref
      (deref [_] "Actually a double deref, because value is a volatile." @value)

      Object
      (equals [this o] (equal? this o Promise #(= @value @o)))
      (hashCode [_] (hash value))
      (toString [_] (str "Promise " value)))))

(declare failed-only future immediate)

(defn- mk-future [value callbacks]
  (reify
    Future
    (await [this milliseconds]
      (letfn
        [(f []
            {:pre [(> milliseconds 0)]}
            (let [phaser (Phaser. 1)]
              (on-complete this (fn [_] (.arriveAndDeregister phaser)))
              (try (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS)
                   (deref value)
                   (catch TimeoutException _
                     (failure (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
                   (catch Throwable e (failure e)))))]
        (f)))
    (on-complete [_ f]
      (let [v @value]
        (if (= v ::incomplete)
          (vswap! callbacks conj f)
          (execute-t f v))))

    IDeref
    (deref [_] "Actually a double deref, because value is a volatile." @value)

    Object
    (equals [this o] (equal? this o Future #(= @value @o)))
    (hashCode [_] (hash value))
    (toString [_] (str "Future " (deref value)))

    Functor
    (-fmap [this f]
      (let [p (promise)]
        (on-complete this (fn [a] (complete p (success? a f identity))))
        (->future p)))

    Pure
    (-pure [_ u] (immediate u))

    ; Todo: causes stack overflow
    ; Applicative
    ; (-apply [m f] ((lift #(% %2)) m f))

    Monad
    (-bind [this f]
      (let [p (promise)
            succ (fn [a] (on-complete (failed-only (f @a)) (fn [b] (complete p b))))
            fail (fn [a] (complete p a))]
        (on-complete this (fn [a] (success? a succ fail)))
        (->future p)))

    ; Todo: add MonadPlus
    ))

;; fn

(defn immediate [v]
  (let [p (promise)]
    (complete p (success v))
    (->future p)))

(defn failed [v]
  (let [p (promise)]
    (complete p (failure v))
    (->future p)))

(defn- failed-only [f]
  {:post [(satisfies? Future %)]}
  (try f (catch Throwable e (failed e))))

;; macro

(defmacro future [& body]
  `(let [p# (promise)]
     (execute-t (fn [] (complete p# (result ~@body))))
     (->future p#)))

;; utility fn

(defn first-result [& fs]
  "Returns the first success or the first failure and discards the rest."
  (let [p (promise)]
    (doseq [f fs]
      (on-complete f (fn [v] (try (complete p v) (catch IllegalStateException _)))))
    (->future p)))

(defn first-success [& fs]
  "Returns the first success or if all fail the last failure."
  (let [p (promise)
        c (volatile! (count fs))
        succ (fn [a] (try (complete p a) (catch IllegalStateException _)))
        fail (fn [a] (when (= 0 (vswap! c dec)) (complete p a)))]
    (doseq [f fs] (on-complete f (fn [a] (success? a succ fail))))
    (->future p)))

;; eof
