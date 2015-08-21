(in-ns 'kite.concurrent)

(import
  [clojure.lang IDeref]
  [java.util.concurrent Phaser TimeoutException TimeUnit])

;; types

(declare immediate failed-only)

(defn- mk-future [value callbacks]
  (reify
    Future
    (await [this milliseconds]
      (letfn
        [(f []
            {:pre [(> milliseconds 0)]}
            (let [phaser (Phaser. 1)]
              (on-complete this (fn [_] (.arriveAndDeregister phaser)))
              (try (.awaitAdvanceInterruptibly
                     phaser
                     0 milliseconds TimeUnit/MILLISECONDS)
                   @value
                   (catch TimeoutException _
                     (failure (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
                   (catch Throwable e
                     (failure e)))))]
        (f)))
    (on-complete [_ f]
      "'on-complete is a ReaderMonad."
      (m-do [env (ask)]
            [:return
             (let [v @value]
               (if (= v not-yet-completed)
                 (conj! callbacks f)
                 (run-reader (execute f v) env)))]))

    IDeref
    (deref [_] "Actually a double deref, because value is a volatile." @value)

    Object
    (equals [this o] (test-eq this o Future #(= @value @o)))
    (hashCode [_] (hash @value))
    (toString [_] (<< "Future  ~{@value}"))

    Functor
    (-fmap [this f]
      (let [p (promise)]
        (on-complete this (fn [a] (complete p (success? a f identity))))
        (->future p)))

    Pure
    (-pure [_ u] (immediate u))

    Monad
    (-bind [this f]
      (let [p (promise)
            succ (fn [a] (on-complete (failed-only (f @a)) (fn [b] (complete p b))))
            fail (fn [a] (complete p a))]
        (on-complete this (fn [a] (success? a succ fail)))
        (->future p)))

    Reader
    (run-reader [this _] (println "run-reader" this))))

;; fn

(defn immediate [v]
  (let [p (promise)]
    (complete p (success v))
    (->future p)))

(comment
  (defn failed [v]
    (m-do [env (ask)]
          [:return
           (let [p ((promise) env)]
             (complete p (failure v))
             (->future p))]))

  (defn- failed-only [f]
    {:post [(satisfies? Future %)]}
    (try f (catch Throwable e (failed e))))

  ;; macro

  (defmacro future [& body]
    `(let [p# (promise)]
       (mock-execute (fn [] (complete p# (result ~@body))))
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
  )

(comment mk-future)

;; eof
