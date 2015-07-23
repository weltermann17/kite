(in-ns 'kite)

(import
  [clojure.lang IDeref]
  [java.util.concurrent Phaser TimeoutException TimeUnit])

(require
  '[clojure.core.strint :refer [<<]])

;; protocols

(defprotocol Future
  (^Try await [_ milliseconds] "Should be used for testing only.")
  (on-complete [_ f]))

(defprotocol Promise
  (complete [_ v])
  (^Future ->future [_]))

;; mocks

(defn- execute
  ([f v] (execute (fn [] (f v))))
  ([f] (f)))

(defn- execute-all [fs v]
  (doseq [f fs] (execute f v)))

;; types

(declare immediate mk-future try-future)

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

(defn lift2
  "Lifts a binary function `f` into a monadic context"
  [f]
  (fn [ma mb]
    (m-do [a ma
           b mb]
          [(pure ma (f a b))])))

(defn- ^Future mk-future [value callbacks]
  (reify
    Future
    (await [m milliseconds]
      (let [phaser (Phaser. 1)]
        (on-complete m (fn [_] (.arriveAndDeregister phaser)))
        (try
          (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS)
          (deref m)
          (catch TimeoutException _
            (failure (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
          (catch Exception e
            (failure e)))))
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
    (-fmap [m f]
      (let [p (promise)]
        (on-complete m (fn [a] (complete p (match-try identity f a))))
        (->future p)))

    Pure
    (-pure [_ u] (println "-pure" u) (immediate u))

    Applicative
    (-ap [m f] (println "-ap") (println m) (println f) ((lift2 #(% %2)) m f)) ;; :TODO: fix stackoverflow

    Monad
    (-bind [_ f] (/ 1 0) :nyi)))

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
