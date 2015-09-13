(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ThreadFactory
    Executors))

;; single-threaded

(defn- default-single-threaded-error-reporter []
  (reader (fn [msg e] (error "single-threaded-error-reporter:" msg ":" e))))

(defn- default-single-threaded-uncaught-exception-handler []
  (m-do [reporter (asks :single-threaded-error-reporter)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (reporter t e)))]))

(defn- default-single-threaded-thread-factory []
  (m-do [uncaught (asks :single-threaded-uncaught-exception-handler)]
        [:let _ (check-type Thread$UncaughtExceptionHandler uncaught)]
        [:return (reify
                   ThreadFactory
                   (^Thread newThread [_ ^Runnable r]
                     (configure-thread (Thread. r) uncaught)))]))

(defn- default-single-threaded-executor []
  (m-do [threadfactory (asks :single-threaded-thread-factory)]
        [:let
         _ (check-type ThreadFactory threadfactory)]
        [:return
         (fn [] (Executors/newSingleThreadExecutor threadfactory))]))

;; eof
