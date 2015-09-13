(in-ns 'kite.execution)

(import
  (java.util.concurrent
    TimeUnit
    ThreadFactory
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
        [:let _ (check-type Thread$UncaughtExceptionHandler uncaught)]
        [:return (reify
                   ThreadFactory
                   (^Thread newThread [_ ^Runnable r]
                     (configure-thread (Thread. r) uncaught)))]))

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
         (fn [] (Executors/newScheduledThreadPool mn threadfactory))]))

;; fn

(defn schedule-once [f milliseconds]
  {:pre [fn? f
         number? milliseconds]}
  (let [scheduler (from-context :scheduler)
        inner-context (all-context)
        inner-f (fn [] (with-context inner-context (f)))]
    (.schedule
      ^ScheduledExecutorService scheduler
      ^Runnable inner-f
      ^Long milliseconds TimeUnit/MILLISECONDS)))

(defn schedule-repeatedly [f initial-delay repeated-delay]
  {:pre [fn? f
         number? initial-delay
         number? repeated-delay]}
  (let [scheduler (from-context :scheduler)
        inner-context (all-context)
        inner-f (fn [] (with-context inner-context (f)))]
    (.scheduleAtFixedRate
      ^ScheduledExecutorService scheduler
      ^Runnable inner-f
      ^Long initial-delay ^Long repeated-delay TimeUnit/MILLISECONDS)))

;; eof
