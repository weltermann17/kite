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
  (reader (fn [msg e] (error "forkjoin-error-reporter:" msg ":" e))))

(defn- default-forkjoin-parallelism-ratio []
  (reader 1.0))

(defn- default-forkjoin-parallelism []
  "Computed from the parallelism ratio and the number of available cores."
  (m-do [ratio (asks :forkjoin-parallelism-ratio)]
        [:let _ (check-type Number ratio)]
        [:return (long (* ratio (number-of-cores)))]))

(defn- default-forkjoin-uncaught-exception-handler []
  (m-do [reporter (asks :forkjoin-error-reporter)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (reporter t e)))]))

(defn- default-forkjoin-thread-factory []
  (reader (reify ForkJoinPool$ForkJoinWorkerThreadFactory
            (^ForkJoinWorkerThread newThread [_ ^ForkJoinPool p]
              (proxy [ForkJoinWorkerThread] [p]
                (onStart []
                  (when (= {} (all-context)) (reset-implicit-context p))
                  (proxy-super onStart)))))))

(defn- default-forkjoin-async-mode []
  (reader true))

(defn- default-forkjoin-recursive-action []
  (reader (fn [f]
            (proxy [RecursiveAction] [] (compute [] (f))))))

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
         (fn [] (proxy [ForkJoinPool] [parallelism
                                       threadfactory
                                       uncaught
                                       async]
                  (shutdown []
                    (let [^ForkJoinPool this this]
                      (remove-implicit-context this)
                      (proxy-super shutdown)))
                  (shutdownNow []
                    (let [^ForkJoinPool this this]
                      (remove-implicit-context this)
                      (proxy-super shutdownNow)))))]))

;; eof
