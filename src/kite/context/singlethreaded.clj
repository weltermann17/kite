(in-ns 'kite.context)

(import
  (java.util.concurrent
    ThreadFactory
    ArrayBlockingQueue
    ThreadPoolExecutor))

;; single-threaded

(defn- default-single-threaded-thread-factory []
  (m-do [uncaught (asks :uncaught-exception-handler)]
        [:return (reify
                   ThreadFactory
                   (^Thread newThread [_ ^Runnable r]
                     (init-thread (Thread. r) uncaught)))]))

(defn- default-single-threaded-executor []
  (reader nil))

;; eof
