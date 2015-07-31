(in-ns 'kite.context)

(import
  (java.util.concurrent
    ThreadFactory
    ArrayBlockingQueue
    ThreadPoolExecutor))

;; threadpool

(defn- default-threadpool-minimum-size []
  (reader (* 1 number-of-cores)))

(defn- default-threadpool-maximum-size []
  (reader (* 8 number-of-cores)))

(defn- default-threadpool-keep-alive []
  "Keep-alive timeout in milliseconds."
  (reader 600000))

(defn- default-threadpool-thread-factory []
  (m-do [uncaught (asks :uncaught-exception-handler)]
        [:return (reify
                   ThreadFactory
                   (^Thread newThread [_ ^Runnable r]
                     (init-thread (Thread. r) uncaught)))]))

(defn- default-threadpool-blocking-queue-capacity []
  (reader (* 32 1024)))

(defn- default-threadpool-blocking-queue []
  (reader nil))

(defn- default-threadpool-executor []
  (reader nil))

;; eof
