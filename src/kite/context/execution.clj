(in-ns 'kite.context)

(import
  (clojure.lang IPersistentMap)
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
  '[kite.category :refer :all]
  '[kite.monad :refer :all])


;; protocols

(defprotocol ExecutorContext
  (^ExecutorService executor [_])
  (^AsynchronousChannelGroup channel-group [_]))

;; many helpers

(defn- default-error-reporter []
  (reader (fn [m ^Throwable e] (println "default-reporter:" m ":" (.toString e)))))

(defn- ^Thread$UncaughtExceptionHandler default-uncaught-exception-handler []
  (m-do [r (asks :error-reporter)
         c (asks :forkjoin-parallelism-factor)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (r (str c t) e)))]))

(defn- default-thread-factory []
  (m-do [^Thread$UncaughtExceptionHandler handler (asks :uncaught-exception-handler)]
        [:return (fn [^String s] (.uncaughtException handler (Thread.) (Exception. s)))]))

(defn- default-forkjoin-parallelism-factor []
  (reader 2.0))

;; configuration

(defrecord ExecutionConfiguration
  [error-reporter
   uncaught-exception-handler
   forkjoin-parallelism-factor
   thread-factory
   ])

(defn- default-execution-configuration []
  (map->ExecutionConfiguration
    {:error-reporter              (default-error-reporter)
     :uncaught-exception-handler  (default-uncaught-exception-handler)
     :forkjoin-parallelism-factor (default-forkjoin-parallelism-factor)
     :thread-factory              (default-thread-factory)
     }))

(defn mk-config [config]
  {:pre  [(instance? IPersistentMap config)]
   :post [(instance? IPersistentMap %)]}
  (let [c (merge (default-execution-configuration) config)
        readers (select-keys c (for [[k v] c :when (satisfies? Reader v)] k))
        run-readers-1 (into {} (for [[k v] readers] [k ((run-reader v) c)]))
        run-readers-2 (into {} (for [[k v] readers] [k ((run-reader v) run-readers-1)]))
        run-readers-3 (into {} (for [[k v] readers] [k ((run-reader v) run-readers-2)]))
        final-config (merge config run-readers-3)
        still-readers (select-keys final-config (for [[k v] final-config :when (satisfies? Reader v)] k))]
    ;(clojure.pprint/pprint run-readers-1)
    ;(clojure.pprint/pprint run-readers-2)
    ;(clojure.pprint/pprint run-readers-3)
    ;(println "final" final-config)
    ;(println "still-readers" still-readers)
    final-config
    ))

;; eof
