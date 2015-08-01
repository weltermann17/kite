(in-ns 'kite.context)

(import
  (java.util.concurrent
    TimeUnit
    ThreadFactory
    ArrayBlockingQueue
    ThreadPoolExecutor
    ThreadPoolExecutor$DiscardPolicy))

;; threadpool

(defn- default-threadpool-error-reporter []
  (reader (fn [msg e] (println "threadpool:" msg ":" e))))

(defn- default-threadpool-uncaught-exception-handler []
  (m-do [reporter (asks :threadpool-error-reporter)]
        [:return
         (reify Thread$UncaughtExceptionHandler
           (uncaughtException [_ t e] (reporter t e)))]))

(defn- default-threadpool-minimum-size []
  (reader (* 1 (number-of-cores))))

(defn- default-threadpool-maximum-size []
  (reader (* 8 (number-of-cores))))

(defn- default-threadpool-keep-alive []
  "Keep-alive timeout in milliseconds."
  (reader 0))

(defn- default-threadpool-thread-factory []
  (m-do [uncaught (asks :threadpool-uncaught-exception-handler)]
        [:return
         (reify
           ThreadFactory
           (^Thread newThread [_ ^Runnable r]
             (set-thread (Thread. r) uncaught)))]))

(defn- default-threadpool-blocking-queue-capacity []
  (reader (* 32 1024)))

(defn- default-threadpool-blocking-queue []
  (m-do [capacity (asks :threadpool-blocking-queue-capacity)]
        [:let _ (check-type Long capacity)]
        [:return
         (proxy [ArrayBlockingQueue] [capacity]
           (offer [e] (proxy-super put e) true))]))

(defn- default-threadpool-executor []
  (m-do [mn (asks :threadpool-minimum-size)
         mx (asks :threadpool-maximum-size)
         keepalive (asks :threadpool-keep-alive)
         queue (asks :threadpool-blocking-queue)
         threadfactory (asks :threadpool-thread-factory)]
        [:let
         _ (check-type Long mn)
         _ (check-type Long mx)
         _ (check-cond (>= mx mn))
         _ (check-type ThreadFactory threadfactory)]
        [:return
         (ThreadPoolExecutor.
           mn
           mx
           keepalive TimeUnit/MILLISECONDS
           queue
           threadfactory
           (ThreadPoolExecutor$DiscardPolicy.))]))

;; eof
