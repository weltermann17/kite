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
         recursive-action (from-context :recursive-action)
         inner-context (all-context)
         inner-f (fn [] (with-context inner-context (f)))]
     (if (instance? ForkJoinPool e)
       (if (ForkJoinTask/inForkJoinPool)
         (.fork ^RecursiveAction (recursive-action inner-f))
         (.execute ^ForkJoinPool e ^Runnable inner-f))
       (.execute e inner-f)))))

(defn execute-all [fs v]
  "This is still a difficult one. Also a ReaderMonad."      ;; todo: unmake a ReaderMonad
  (m-do [env (ask)]
        [:let fs' (map #(execute % v) fs)]                  ; fmap 20x slower
        [:return (do (println "execute-all" fs v) (doseq [f fs'] (run-reader f env)))])) ; only want the side-effects of all fs

(defn execute-blocking [] nil)

;; eof
