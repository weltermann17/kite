(in-ns 'kite.context)

(import
  (java.util.concurrent
    ArrayBlockingQueue
    RejectedExecutionHandler
    ThreadFactory
    ThreadPoolExecutor
    ThreadPoolExecutor$DiscardPolicy
    TimeUnit))

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
  (reader (* 2 (number-of-cores))))

(defn- default-threadpool-keep-alive []
  "Keep-alive timeout in milliseconds."
  (reader 0))

(defn- default-threadpool-thread-factory []
  (m-do [uncaught (asks :threadpool-uncaught-exception-handler)]
        [:return (reify
                   ThreadFactory
                   (^Thread newThread [_ ^Runnable r]
                     (set-thread (Thread. r) uncaught)))]))

(defn- default-threadpool-blocking-queue-capacity []
  (reader (* 32 1024)))

(defn- default-threadpool-blocking-queue []
  "Contains a hack to type-hint 'this' before calling proxy-super.
  This will only work for public methods on super, though."
  (m-do [capacity (asks :threadpool-blocking-queue-capacity)]
        [:let _
         (check-type Long capacity)]
        [:return
         (proxy [ArrayBlockingQueue] [capacity]
           (offer [e]
             (let [^ArrayBlockingQueue this this]
               (comment this)
               (proxy-super put e) true)))]))

(defn- default-threadpool-rejection-policy []
  (reader (ThreadPoolExecutor$DiscardPolicy.)))

(defn- default-threadpool-executor []
  (m-do [mn (asks :threadpool-minimum-size)
         mx (asks :threadpool-maximum-size)
         keepalive (asks :threadpool-keep-alive)
         queue (asks :threadpool-blocking-queue)
         threadfactory (asks :threadpool-thread-factory)
         rejection (asks :threadpool-rejection-policy)]
        [:let
         _ (check-type Long mn)
         _ (check-type Long mx)
         _ (check-cond (>= mn 0))
         _ (check-cond (>= mx mn))
         _ (check-type ThreadFactory threadfactory)
         _ (check-type RejectedExecutionHandler rejection)]
        [:return
         (ThreadPoolExecutor.
           mn
           mx
           keepalive TimeUnit/MILLISECONDS
           queue
           threadfactory
           rejection)]))

;; eof
