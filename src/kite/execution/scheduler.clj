(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ThreadFactory
    Executors))

;; scheduler

(defn- default-scheduler-error-reporter []
  (reader (fn [msg e] (println "scheduler:" msg ":" e))))

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
                     (set-thread (Thread. r) uncaught)))]))

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

;; eof
