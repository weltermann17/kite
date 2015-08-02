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

;; the main thing
;; execute implementation

(defn- recursive-action [f]
  (proxy [RecursiveAction] [] (compute [] (f))))

(defn execute
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (m-do [^ExecutorService e (asks :executor)]
         [:return
          (if (instance? ForkJoinPool e)
            (if (ForkJoinTask/inForkJoinPool)
              (.fork ^RecursiveAction (recursive-action f))
              (.execute ^ForkJoinPool e ^Runnable f))
            (.execute e f))])))

;; eof
