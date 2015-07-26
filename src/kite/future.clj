(in-ns 'kite)

(import
  [java.util.concurrent Phaser TimeoutException TimeUnit])

(require
  '[kite.control :refer :all])

;; protocols

(defprotocol Future
  (^Result await [_ milliseconds]
    "Should be used for testing only. Milliseconds must be > 0.")
  (on-complete [_ f]))

(defprotocol Promise
  (complete [_ v])
  (^Future ->future [_]))

;; types

(declare mk-future)

(defn ^Promise promise []
  (let [value (volatile! ::incomplete)
        callbacks (volatile! [])
        future (mk-future value callbacks)]
    (reify
      Promise
      (complete [_ v]
        (if (= @value ::incomplete)
          (do (vreset! value v) (execute-all @callbacks v))
          (illegal-state! (<< "A promise cannot be completed more than once, value = ~{@value}, value not accepted = ~{v}"))))
      (->future [_] future)

      IDeref
      (deref [_] "Actually a double deref, because value is a volatile." @value)

      Object
      (equals [this o] (equal? this o Promise #(= @value @o)))
      (hashCode [_] (hash value))
      (toString [_] (str "Promise " value)))))

(declare failed-only future immediate)

(defn- ^Future mk-future [value callbacks]
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
          (execute f v))))

    IDeref
    (deref [_] "Actually a double deref, because value is a volatile." @value)

    Object
    (equals [this o] (equal? this o Future #(= @value @o)))
    (hashCode [_] (hash value))
    (toString [_] (str "Future " (deref value)))

    Functor
    (-fmap [this f]
      (let [p (promise)]
        (on-complete this (fn [a] (complete p (match-result a f identity))))
        (->future p)))

    Pure
    (-pure [_ u] (immediate u))

    ;Applicative
    ;(-apply [m f] ((lift #(% %2)) m f))

    Monad
    (-bind [this f]
      (let [p (promise)]
        (on-complete
          this
          (fn [a]
            {:pre [(satisfies? Result a)]}
            (matchm a
                    {Success v} (on-complete (failed-only (f v)) (fn [b] (complete p b)))
                    {Failure _} (complete p a))))
        (->future p)))))

;; fn

(defn ^Future immediate [v]
  (let [p (promise)]
    (complete p (success v))
    (->future p)))

(defn ^Future failed [v]
  (let [p (promise)]
    (complete p (failure v))
    (->future p)))

(defn- ^Future failed-only [f]
  {:post [(satisfies? Future %)]}
  (try f (catch Throwable e (failed e))))

;; macro

(defmacro ^Future future [& body]
  `(let [p# (promise)]
     (execute (fn [] (complete p# (result ~@body))))
     (->future p#)))

;; utilities

(defn ambiguous [& fs]
  (let [p (promise)]
    (doseq [f fs]
      (on-complete f (fn [v] (try (complete p v) (catch IllegalStateException _)))))
    (->future p)))

;; eof
