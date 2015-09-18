(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ExecutorService
    ForkJoinPool
    ForkJoinTask
    RecursiveAction))

;; common stuff

(defn- configure-thread [^Thread t ^Thread$UncaughtExceptionHandler uncaught]
  (doto t
    (.setDaemon true)
    (.setUncaughtExceptionHandler uncaught)))

(defn- ^Long number-of-cores []
  (.availableProcessors (Runtime/getRuntime)))

;; the main thing: execute implementations

(defn execute
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (let [executor (from-context :executor)]
     (if (instance? ForkJoinPool executor)
       ;(if (ForkJoinTask/inForkJoinPool)
       ;  (let [recursive-action (from-context :recursive-action)]
       ;    (.fork ^RecursiveAction (recursive-action f)))
       (.execute ^ForkJoinPool executor ^Runnable f)        ;)
       (.execute ^ExecutorService executor f)))))

(defn execute-blocking
  "Use this when f is likely to call blocking code like jdbc or old io."
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (let [executor (from-context :executor)]
     (if (instance? ForkJoinPool executor)
       (let [managed-blocker (from-context :managed-blocker)
             blocking-f (fn [] (ForkJoinPool/managedBlock (managed-blocker f)))]
         (if (ForkJoinTask/inForkJoinPool)
           (let [recursive-action (from-context :recursive-action)]
             (.fork ^RecursiveAction (recursive-action blocking-f)))
           (.execute ^ForkJoinPool executor ^Runnable blocking-f))
         (.execute ^ExecutorService executor f))))))

(comment execute-blocking)

(defn execute-all [fs v]
  ; ((first fs) v))
  ; (doseq [f fs] (f v)))
  (dotimes [i (count fs)] ((nth fs i) v))
  )

;; eof
