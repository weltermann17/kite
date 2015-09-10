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

(defn- ^Long number-of-cores []
  (.availableProcessors (Runtime/getRuntime)))

;; the main thing: execute implementations

(defn execute
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (let [executor (from-context :executor)
         inner-context (all-context)
         inner-f (fn [] (with-context inner-context (f)))]
     (if (instance? ForkJoinPool executor)
       (if (ForkJoinTask/inForkJoinPool)
         (let [recursive-action (from-context :recursive-action)]
           (.fork ^RecursiveAction (recursive-action inner-f)))
         (.execute ^ForkJoinPool executor ^Runnable inner-f))
       (.execute ^ExecutorService executor inner-f)))))

(defn execute-blocking
  "Use this when f is likely to call blocking code."
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (let [executor (from-context :executor)
         inner-context (all-context)
         inner-f (fn [] (with-context inner-context (f)))]
     (if (instance? ForkJoinPool executor)
       (let [managed-blocker (from-context :managed-blocker)
             blocking-f (fn [] (ForkJoinPool/managedBlock (managed-blocker inner-f)))]
         (if (ForkJoinTask/inForkJoinPool)
           (let [recursive-action (from-context :recursive-action)]
             (.fork ^RecursiveAction (recursive-action blocking-f)))
           (.execute ^ForkJoinPool executor ^Runnable blocking-f))
         (.execute ^ExecutorService executor inner-f))))))

(defn execute-all [fs v]
  (doseq [f fs] (execute f v)))

(comment execute-blocking)

;; eof
