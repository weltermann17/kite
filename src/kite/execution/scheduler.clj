(in-ns 'kite.execution)

(import
  (java.util.concurrent
    TimeUnit
    Executors
    ScheduledExecutorService))

;; scheduler

(defn- default-scheduler-error-reporter []
  (reader (fn [msg e] (error "scheduler-error-reporter:" msg ":" e))))

(defn- default-scheduler-uncaught-exception-handler []
  (m-do [reporter (asks :scheduler-error-reporter)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (reporter t e)))]))

(defn- default-scheduler-thread-factory []
  (m-do [uncaught (asks :scheduler-uncaught-exception-handler)]
        [:return (default-thread-factory uncaught)]))

(defn- default-scheduler-minimum-size []
  (reader 0))

(defn- default-scheduler-executor []
  (m-do [mn (asks :scheduler-minimum-size)
         threadfactory (asks :scheduler-thread-factory)]
        [:let
         _ (check-type Long mn)
         _ (check-cond (>= mn 0))
         _ (check-type ThreadFactory threadfactory)]
        [:return
         (fn [] (set-executor threadfactory
                              (Executors/newScheduledThreadPool mn threadfactory)))]))

;; fn

(defn schedule-once [initial-delay f & args]
  "Returns a ScheduledFuture that can be cancelled."
  {:pre [fn? f
         number? initial-delay
         (> initial-delay 0)]}
  (let [scheduler (from-context :scheduler)]
    (.schedule
      ^ScheduledExecutorService scheduler
      ^Runnable (fn [] (apply f args))
      ^Long initial-delay TimeUnit/MILLISECONDS)))

(defn schedule-repeatedly [initial-delay repeated-delay f & args]
  {:pre [fn? f
         number? initial-delay
         number? repeated-delay
         (> initial-delay 0)
         (> repeated-delay 0)]}
  (let [scheduler (from-context :scheduler)]
    (.scheduleAtFixedRate
      ^ScheduledExecutorService scheduler
      ^Runnable (fn [] (apply f args))
      ^Long initial-delay ^Long repeated-delay TimeUnit/MILLISECONDS)))

;; eof
