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

;; the main thing, execute implementation

(defn execute
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (m-do [^ExecutorService e (asks :executor)
          action (asks :recursive-action)]
         [:return
          (if (instance? ForkJoinPool e)
            (if (ForkJoinTask/inForkJoinPool)
              (.fork ^RecursiveAction (action f))
              (.execute ^ForkJoinPool e ^Runnable f))
            (.execute e f))])))

(defn execute-all [fs v]
  "This was a difficult one."
  (m-do [env (ask)]
        [:let fs' (map #(execute % v) fs)]                  ; fmap 20x slower
        [:return (doseq [f fs'] (run-reader f env))]))      ; only want the side-effects of all fs

;; eof
