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
  (reader-m (fn [m e] (println "default-reporter:" m ":" e))))

(defn- default-uncaught-exception-handler []
  (m-do [r (asks :error-reporter)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (r t e)))]))

(defn- default-forkjoin-parallelism-factor []
  (reader-m 2.0))

;; configuration

(defrecord ExecutionConfiguration
  [error-reporter
   uncaught-exception-handler
   forkjoin-parallelism-factor
   ])

(defn- default-execution-configuration []
  (map->ExecutionConfiguration
    {:error-reporter              (default-error-reporter)
     :uncaught-exception-handler  (default-uncaught-exception-handler)
     :forkjoin-parallelism-factor (default-forkjoin-parallelism-factor)
     }))

(defn test1 [config]
  ((run-reader (:forkjoin-parallelism-factor (default-execution-configuration))) config))

(defn test2 [config]
  ((run-reader (:error-reporter (default-execution-configuration))) config))

(defn test3 [c1]
  (let [c2
        {:error-reporter
         ((run-reader
            (:error-reporter (default-execution-configuration))) c1)}]
    ((run-reader
       (:uncaught-exception-handler (default-execution-configuration))) c2)))

(defn mk-config [config]
  {:pre  [(instance? IPersistentMap config)]
   :post [(instance? IPersistentMap %)]}
  (let [c (merge (default-execution-configuration) config)
        readers (select-keys c (for [[k v] c :when (satisfies? Reader v)] k))
        run-readers (into {} (for [[k v] readers] [k ((run-reader v) c)]))
        run-readers-2 (into {} (for [[k v] readers] [k ((run-reader v) run-readers)]))
        final-config (merge config run-readers-2)
        still-readers (select-keys final-config (for [[k v] final-config :when (satisfies? Reader v)] k))]
    ;(println "c" c)
    ;(println "readers" readers)
    (println "run-readers" run-readers)
    (println "run-readers-2" run-readers-2)
    ;(println "final" final-config)
    ;(println "still-readers" still-readers)
    final-config
    ))

;; eof
