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
   ; Any kind of 'java.lang.concurrent.ExecutorService' can be provided directly.
   ; This will override all other configuration settings.
   ; Others (cached-thread-pool, fixed-thread-pool) can be provided via 'executor'.
   executor
   ; Choose policy as one of :forkjoin (default) :threadpool :single-threaded
   executor-policy
   ; common stuff
   error-reporter
   uncaught-exception-handler
   ; forkjoin
   forkjoin-parallelism-factor
   forkjoin-parallelism
   forkjoin-thread-factory
   forkjoin-async-mode
   forkjoin-executor
   ; threadpool
   threadpool-minimum-size
   threadpool-maximum-size
   threadpool-thread-factory
   threadpool-keep-alive-time-in-milliseconds
   threadpool-blocking-queue-maximum-capacity
   threadpool-blocking-queue
   threadpool-executor
   ; single-threaded for testing purposes only
   single-threaded-thread-factory
   ])

(defn- default-executor-policy []
  "One of :forkjoin :threadpool :single-threaded"
  :forkjoin)

(defn default-execution-configuration []
  (map->ExecutionConfiguration
    {:executor-policy                    (default-executor-policy)
     :error-reporter                     (default-error-reporter)
     :uncaught-exception-handler         (default-uncaught-exception-handler)
     :forkjoin-parallelism-factor        (default-forkjoin-parallelism-factor)
     :forkjoin-parallelism               (default-forkjoin-parallelism)
     :forkjoin-thread-factory            (default-forkjoin-thread-factory)
     :forkjoin-async-mode                (default-forkjoin-async-mode)
     :forkjoin-executor                  (default-forkjoin-executor)
     :threadpool-minimum-size            (default-threadpool-minimum-size)
     :threadpool-maximum-size            (default-threadpool-maximum-size)
     :threadpool-thread-factory          (default-threadpool-thread-factory)
     :threadpool-keep-alive              (default-threadpool-keep-alive)
     :threadpool-blocking-queue-capacity (default-threadpool-blocking-queue-capacity)
     :threadpool-blocking-queue          (default-threadpool-blocking-queue)
     :threadpool-executor                (default-threadpool-executor)

     }))

;; eof
