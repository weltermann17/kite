(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ExecutorService
    ForkJoinPool
    ForkJoinTask
    RecursiveAction))

;; common stuff

(defn- set-thread [^Thread t ^Thread$UncaughtExceptionHandler uncaught]
  (doto t
    (.setDaemon true)
    (.setUncaughtExceptionHandler uncaught)))

(defn- ^Integer number-of-cores []
  (.availableProcessors (Runtime/getRuntime)))

;; the main thing, execute* implementation

(defn execute
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (let [^ExecutorService e (from-context :executor)
         action (from-context :recursive-action)]
     (println "context" (from-context :config))
     (println "executor" e)
     (if (instance? ForkJoinPool e)
       (if (ForkJoinTask/inForkJoinPool)
         (.fork ^RecursiveAction (action f))
         (.execute ^ForkJoinPool e ^Runnable f))
       (.execute e f)))))

(defn execute-all [fs v]
  "This is still a difficult one. Also a ReaderMonad."
  (m-do [env (ask)]
        [:let fs' (map #(execute % v) fs)]                  ; fmap 20x slower
        [:return (do (println "execute-all" fs v) (doseq [f fs'] (run-reader f env)))])) ; only want the side-effects of all fs

;; eof
