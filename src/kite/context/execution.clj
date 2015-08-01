(in-ns 'kite.context)

(import
  (java.util.concurrent
    ExecutorService)
  (java.nio.channels
    AsynchronousChannelGroup))

;; protocols

(defprotocol ExecutorContext
  (^ExecutorService executor [_])
  (^AsynchronousChannelGroup channel-group [_]))

;; configuration

(defrecord
  ^{:doc "A doc string."}
  ExecutionConfiguration
  [
   executor
   executor-policy
   forkjoin-error-reporter
   forkjoin-uncaught-exception-handler
   forkjoin-parallelism-factor
   forkjoin-parallelism
   forkjoin-thread-factory
   forkjoin-async-mode
   forkjoin-executor
   threadpool-error-reporter
   threadpool-uncaught-exception-handler
   threadpool-minimum-size
   threadpool-maximum-size
   threadpool-thread-factory
   threadpool-keep-alive
   threadpool-blocking-queue-capacity
   threadpool-blocking-queue
   threadpool-executor
   single-threaded-error-reporter
   single-threaded-uncaught-exception-handler
   single-threaded-thread-factory
   single-threaded-executor
   ])

(defn- default-executor []
  (m-do [policy (asks :executor-policy)
         executor (case policy
                    :forkjoin (asks :forkjoin-executor)
                    :threadpool (asks :threadpool-executor)
                    :single-threaded (asks :single-threaded-executor)
                    (invalid-config! policy ":executor-policy must be one of :forkjoin :threadpool :single-threaded"))]
        [:return executor]))

(defn- default-executor-policy []
  "One of :forkjoin :threadpool :single-threaded"
  (reader :forkjoin))

(defn default-execution-configuration []
  (map->ExecutionConfiguration
    {
     :executor                                   (default-executor)
     :executor-policy                            (default-executor-policy)
     :forkjoin-error-reporter                    (default-forkjoin-error-reporter)
     :forkjoin-uncaught-exception-handler        (default-forkjoin-uncaught-exception-handler)
     :forkjoin-parallelism-factor                (default-forkjoin-parallelism-factor)
     :forkjoin-parallelism                       (default-forkjoin-parallelism)
     :forkjoin-thread-factory                    (default-forkjoin-thread-factory)
     :forkjoin-async-mode                        (default-forkjoin-async-mode)
     :forkjoin-executor                          (default-forkjoin-executor)
     :threadpool-error-reporter                  (default-threadpool-error-reporter)
     :threadpool-uncaught-exception-handler      (default-threadpool-uncaught-exception-handler)
     :threadpool-minimum-size                    (default-threadpool-minimum-size)
     :threadpool-maximum-size                    (default-threadpool-maximum-size)
     :threadpool-thread-factory                  (default-threadpool-thread-factory)
     :threadpool-keep-alive                      (default-threadpool-keep-alive)
     :threadpool-blocking-queue-capacity         (default-threadpool-blocking-queue-capacity)
     :threadpool-blocking-queue                  (default-threadpool-blocking-queue)
     :threadpool-executor                        (default-threadpool-executor)
     :single-threaded-error-reporter             (default-single-threaded-error-reporter)
     :single-threaded-uncaught-exception-handler (default-single-threaded-uncaught-exception-handler)
     :single-threaded-thread-factory             (default-single-threaded-thread-factory)
     :single-threaded-executor                   (default-single-threaded-executor)
     }))

;; eof
