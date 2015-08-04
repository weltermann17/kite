(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ForkJoinPool
    ForkJoinPool$ForkJoinWorkerThreadFactory
    ForkJoinWorkerThread
    ForkJoinPool$ManagedBlocker
    RecursiveAction))

;; forkjoin

(defn- default-forkjoin-error-reporter []
  (reader (fn [msg e] (println "forkjoin:" msg ":" e))))

(defn- default-forkjoin-uncaught-exception-handler []
  (m-do [reporter (asks :forkjoin-error-reporter)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (reporter t e)))]))

(defn- default-forkjoin-parallelism-factor []
  (reader 2.0))

(defn- default-forkjoin-parallelism []
  "Computed from the parallelism factor and the number of available cores."
  (m-do [fac (asks :forkjoin-parallelism-factor)]
        [:let _ (check-type Number fac)]
        [:return (long (* fac (number-of-cores)))]))

(defn- default-forkjoin-thread-factory []
  (m-do [uncaught (asks :forkjoin-uncaught-exception-handler)]
        [:return
         (reify
           ForkJoinPool$ForkJoinWorkerThreadFactory
           (^ForkJoinWorkerThread newThread [_ ^ForkJoinPool p]
             (set-thread (proxy [ForkJoinWorkerThread] [p]) uncaught)))]))

(defn- default-forkjoin-async-mode []
  (reader true))

(defn- default-forkjoin-recursive-action []
  (reader (fn [f] (proxy [RecursiveAction] [] (compute [] (f))))))

(defn- default-forkjoin-managed-blocker []
  (reader (fn [f]
            (let [done (volatile! false)]
              (reify ForkJoinPool$ManagedBlocker
                (block [_] (try (f) (finally (vreset! done true))) true)
                (isReleasable [_] @done))))))

(defn- default-forkjoin-executor []
  (m-do [parallelism (asks :forkjoin-parallelism)
         threadfactory (asks :forkjoin-thread-factory)
         uncaught (asks :forkjoin-uncaught-exception-handler)
         async (asks :forkjoin-async-mode)]
        [:let
         _ (check-type Long parallelism)
         _ (check-type ForkJoinPool$ForkJoinWorkerThreadFactory threadfactory)
         _ (check-type Thread$UncaughtExceptionHandler uncaught)
         _ (check-type Boolean async)
         _ (check-cond (>= parallelism 1))]
        [:return
         (fn [] (ForkJoinPool.
                  parallelism
                  threadfactory
                  uncaught
                  async))]))

;; eof
