(in-ns 'kite)

(import
  [clojure.lang IDeref]
  [java.util.concurrent Phaser TimeoutException TimeUnit])

(refer-clojure :exclude '[await future promise])

(require
  '[clojure.core.strint :refer [<<]])

(defprotocol Promise
  (complete [_ v])
  (->future [_]))

(defprotocol Future
  (await [_ milliseconds])
  (on-failure [_ f])
  (on-success [_ f])
  (on-complete [_ f]))

(defn- failed-future [v] nil)

(defn- execute [f v] nil)
(defn- execute-all [f v] nil)

(defn- mk-future [v callbacks]
  (reify
    Future
    (await [this milliseconds]
      (let [phaser (Phaser. 1)]
        (on-complete this (fn [_] (.arriveAndDeregister phaser)))
        (try
          (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS)
          this
          (catch TimeoutException _
            (failed-future (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
          (catch Exception e
            (failed-future e)))))
    (on-complete [_ f]
      (let [v @value]
        (if (= v ::incomplete)
          (vswap! callbacks conj f)
          (execute f v))))
    (on-failure [this f] (on-complete this (fn [v] (match-try (comp f deref) nil v))))
    (on-success [this f] (on-complete this (fn [v] (match-try nil (comp f deref) v))))

    IDeref
    (deref [_] @v)

    Object
    (equals [_ o] (and (satisfies? Future o) (= v (deref o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Future " v))))

(defn promise []
  (let [v (volatile! ::incomplete)
        callbacks (volatile! [])
        fut (mk-future v callbacks)]
    (reify
      Promise
      (complete [_ v]
        (if (= @value ::incomplete)
          (do (vreset! value v) (execute-all @callbacks v))
          (let [^String s (<< "A promise cannot be completed more than once, value = ~{value}, not accepted value = ~{v}")]
            (throw (IllegalStateException. s)))))
      (->future [_] fut)

      IDeref
      (deref [_] @value)

      Object
      (equals [_ o] (and (satisfies? Promise o) (= v (deref o))))
      (hashCode [_] (hash v))
      (toString [_] (str "Promise " v)))))

(comment promise)

;; eof
