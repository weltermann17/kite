(in-ns 'kite.context)

(import
  (java.util.concurrent
    ExecutorService))

;; configuration

(defn- default-executor []
  (m-do [policy (asks :executor-policy)
         executor (case policy
                    :forkjoin (asks :forkjoin-executor)
                    :threadpool (asks :threadpool-executor)
                    :single-threaded (asks :single-threaded-executor)
                    (invalid-config! policy "Unknown executor-policy"))]
        [:return executor]))

(defn- default-executor-policy []
  "One of :forkjoin :threadpool :single-threaded"
  (reader :forkjoin))

(defn default-execution-configuration []
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
   :threadpool-rejection-policy                (default-threadpool-rejection-policy)
   :threadpool-executor                        (default-threadpool-executor)
   :single-threaded-error-reporter             (default-single-threaded-error-reporter)
   :single-threaded-uncaught-exception-handler (default-single-threaded-uncaught-exception-handler)
   :single-threaded-thread-factory             (default-single-threaded-thread-factory)
   :single-threaded-executor                   (default-single-threaded-executor)
   })

;; context

(defprotocol BaseContext
  (get-config [_])
  (set-config [_ v]))

(deftype Context [^:unsynchronized-mutable config]
  BaseContext
  (get-config [_] config)
  (set-config [this v] (set! config v) this))

(defprotocol ExecutorContext
  (^ExecutorService executor [_]))

(defn add-executor-context [context initial-config]
  (let [c (mk-config (default-execution-configuration) initial-config)
        e (:executor c)]
    (extend-type Context
      ExecutorContext
      (executor [_] e))
    (set-config context c)))

;; eof
