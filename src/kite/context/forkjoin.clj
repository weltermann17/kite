(in-ns 'kite.context)

(import
  (java.lang
    Thread$UncaughtExceptionHandler)
  (java.util.concurrent
    ForkJoinPool
    ForkJoinPool$ForkJoinWorkerThreadFactory
    ForkJoinTask
    ForkJoinWorkerThread
    ForkJoinPool$ManagedBlocker
    RecursiveAction))

;; forkjoin

(defn- default-forkjoin-parallelism-factor []
  (reader 2.0))

(defn- default-forkjoin-parallelism []
  "Computed from the parallelism factor and the number of available cores."
  (m-do [fac (asks :forkjoin-parallelism-factor)]
        [:let _ (valid-type?! Number fac)]
        [:return (int (* fac number-of-cores))]))

(defn- default-forkjoin-thread-factory []
  (m-do [uncaught (asks :uncaught-exception-handler)]
        [:return
         (reify
           ForkJoinPool$ForkJoinWorkerThreadFactory
           (^ForkJoinWorkerThread newThread [_ ^ForkJoinPool p]
             (init-thread (proxy [ForkJoinWorkerThread] [p]) uncaught)))]))

(defn- default-forkjoin-async-mode []
  (reader true))

(defn- default-forkjoin-executor []
  (m-do [parallelism (asks :forkjoin-parallelism)
         threadfactory (asks :forkjoin-thread-factory)
         uncaught (asks :uncaught-exception-handler)
         async (asks :forkjoin-async-mode)]
        [:return (ForkJoinPool. (max 2 parallelism) threadfactory uncaught async)]))

;; eof
