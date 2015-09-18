(in-ns 'kite.execution)

(import
  (java.util.concurrent
    ExecutorService
    ForkJoinPool
    ForkJoinTask
    RecursiveAction
    ThreadFactory))

;; common stuff

(defn- ^Long number-of-cores []
  (.availableProcessors (Runtime/getRuntime)))

(defn- ^ThreadFactory default-thread-factory
  [^Thread$UncaughtExceptionHandler uncaught]
  (reify
    ThreadFactory
    (^Thread newThread [_ ^Runnable r]
      (doto (proxy [Thread] [r]
              (run []
                (let [^Thread this this
                      ^ExecutorService executor (from-context :executor)]
                  (comment this)
                  (when (= {} (all-context)) (reset-implicit-context executor))
                  (assert (not= {} (all-context)))
                  (proxy-super run))))
        (.setDaemon true)
        (.setUncaughtExceptionHandler uncaught)))))

;; the main thing: execute implementations

(defn execute
  ([f v]
   (execute (fn [] (f v))))
  ([f]
   (let [executor (from-context :executor)]
     (if (instance? ForkJoinPool executor)
       (if (ForkJoinTask/inForkJoinPool)
         (let [recursive-action (from-context :recursive-action)]
           (.fork ^RecursiveAction (recursive-action f)))
         (.execute ^ForkJoinPool executor ^Runnable f))
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
  "'doseq' is a performance killer, therefore this ugly code."
  (let [c (count fs)]
    (if (== 2 c)
      (do ((first fs) v) (second fs) v)
      (if (== 1 c)
        ((first fs) v)
        (doseq [f fs] (f v))))))

;; eof
