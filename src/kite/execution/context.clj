(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ExecutorService
    ScheduledExecutorService))

;; configuration

(defn- default-executor []
  "Remember, 'executor' is a arity-0 fn."
  (m-do [policy (asks :executor-policy)
         executor (case policy
                    :forkjoin (asks :forkjoin-executor)
                    :threadpool (asks :threadpool-executor)
                    :single-threaded (asks :single-threaded-executor)
                    (invalid-config! policy "Unknown executor-policy"))]
        [:return executor]))

(defn- default-executor-policy []
  "Must be one in #{:forkjoin :threadpool :single-threaded}."
  (reader :forkjoin))

(defn default-execution-configuration []
  {
   :executor                                   (default-executor)
   :executor-policy                            (default-executor-policy)
   :forkjoin-error-reporter                    (default-forkjoin-error-reporter)
   :forkjoin-uncaught-exception-handler        (default-forkjoin-uncaught-exception-handler)
   :forkjoin-parallelism-ratio                 (default-forkjoin-parallelism-ratio)
   :forkjoin-parallelism                       (default-forkjoin-parallelism)
   :forkjoin-thread-factory                    (default-forkjoin-thread-factory)
   :forkjoin-async-mode                        (default-forkjoin-async-mode)
   :forkjoin-recursive-action                  (default-forkjoin-recursive-action)
   :forkjoin-managed-blocker                   (default-forkjoin-managed-blocker)
   :forkjoin-executor                          (default-forkjoin-executor)
   :threadpool-error-reporter                  (default-threadpool-error-reporter)
   :threadpool-uncaught-exception-handler      (default-threadpool-uncaught-exception-handler)
   :threadpool-minimum-size                    (default-threadpool-minimum-size)
   :threadpool-maximum-size                    (default-threadpool-maximum-size)
   :threadpool-thread-factory                  (default-threadpool-thread-factory)
   :threadpool-keep-alive                      (default-threadpool-keep-alive)
   :threadpool-blocking-queue-capacity         (default-threadpool-blocking-queue-capacity)
   :threadpool-blocking-queue                  (default-threadpool-blocking-queue)
   :threadpool-rejection-policy                (default-threadpool-rejection-policy)
   :threadpool-executor                        (default-threadpool-executor)
   :single-threaded-error-reporter             (default-single-threaded-error-reporter)
   :single-threaded-uncaught-exception-handler (default-single-threaded-uncaught-exception-handler)
   :single-threaded-thread-factory             (default-single-threaded-thread-factory)
   :single-threaded-executor                   (default-single-threaded-executor)
   :scheduler-error-reporter                   (default-scheduler-error-reporter)
   :scheduler-uncaught-exception-handler       (default-scheduler-uncaught-exception-handler)
   :scheduler-thread-factory                   (default-scheduler-thread-factory)
   :scheduler-minimum-size                     (default-scheduler-minimum-size)
   :scheduler-executor                         (default-scheduler-executor)
   })

;; context

(defn add-executor-context [context initial-config]
  (let [c (merge-config (default-execution-configuration) initial-config)
        ^ExecutorService e ((:executor c))
        ^ScheduledExecutorService s ((:scheduler-executor c))]
    (merge context
           {:config    c
            :executor  e
            :scheduler s}
           (when (instance? ForkJoinPool e)
             {:recursive-action (:forkjoin-recursive-action c)
              :managed-blocker  (:forkjoin-managed-blocker c)}))))

;; eof
