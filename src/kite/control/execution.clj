(in-ns 'kite.control)

(import
  (java.lang
    Thread$UncaughtExceptionHandler)
  (java.util.concurrent
    Executor
    ExecutorService
    ForkJoinPool
    ThreadFactory
    ForkJoinPool$ForkJoinWorkerThreadFactory
    ForkJoinTask
    ForkJoinWorkerThread
    ForkJoinPool$ManagedBlocker
    RecursiveAction)
  [java.nio.channels
   AsynchronousChannelGroup])

(require
  '[clojure.core.strint :refer [<<]]
  '[kite.category :refer :all])

;; protocols

(defprotocol ExecutorContext
  (^ExecutorService executor [_])
  (^AsynchronousChannelGroup channel-group [_]))

;; many helpers

(defn- default-uncaught-exception-handler []
  (reify Thread$UncaughtExceptionHandler (uncaughtException [_ _ e] (println e))))







;; mocks and helpers

(defn execute
  ([f v] (execute (fn [] (f v))))
  ([f] (f)))

(defn execute-all [fs v]
  (doseq [f fs] (execute f v)))

;; eof
