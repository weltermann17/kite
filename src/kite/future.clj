(in-ns 'kite)

(import
  [java.util.concurrent Phaser TimeoutException TimeUnit])

(require
  '[kite.control :refer :all])

;; protocols

(defprotocol Future
  (^Result await [_ milliseconds]
    "Should be used for testing only.")
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
          (illegal-state! (<< "A promise cannot be completed more than once, value = ~{value}, value not accepted = ~{v}"))))
      (->future [_] future)

      IDeref
      (deref [_] @value)

      Object
      (equals [this o] (or (identical? this o) (and (satisfies? Promise o) (= value (deref o)))))
      (hashCode [_] (hash value))
      (toString [_] (str "Promise " value)))))

(declare failed-only immediate)

(defn- ^Future mk-future [value callbacks]
  (reify
    Future
    (await [m milliseconds]
      (let [phaser (Phaser. 1)]
        (on-complete m (fn [_] (.arriveAndDeregister phaser)))
        (try (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS)
             (deref m)
             (catch TimeoutException _
               (failure (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
             (catch Throwable e (failure e)))))
    (on-complete [_ f]
      (let [v @value]
        (if (= v ::incomplete)
          (vswap! callbacks conj f)
          (execute f v))))

    IDeref
    (deref [_] @value)

    Object
    (equals [this o] (equal? this o Right #(= value @o)))
    (hashCode [_] (hash value))
    (toString [_] (str "Future " (deref value)))

    Functor
    (-fmap [m f]
      (let [p (promise)]
        (on-complete m (fn [a] (complete p (match-result a f identity))))
        (->future p)))

    Pure
    (-pure [_ u] (immediate u))

    Applicative
    (-ap [m f] ((lift #(% %2)) m f))

    Monad
    (-bind [m f]
      (let [p (promise)]
        (on-complete
          m (fn [a]
              (if (satisfies? Success a)
                (on-complete (failed-only (f (deref a))) (fn [b] (complete p b)))
                (complete p a))))
        (->future p)))))

;; fn

(defn ^Future future-fn [f]
  (let [p (promise)]
    (execute (fn [] (complete p (result-fn f))))
    (->future p)))

(defn ^Future immediate [v]
  (let [p (promise)]
    (complete p (success v))
    (->future p)))

(defn ^Future failed [v]
  (let [p (promise)]
    (complete p (failure v))
    (->future p)))

(defn- failed-only [f]
  (try f (catch Throwable e (failed e))))

;; macros

(defmacro ^Future future [& body]
  `(future-fn (fn [] ~@body)))

;; eof
