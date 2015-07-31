(in-ns 'kite.context)

(defn- default-error-reporter []
  (reader (fn [m ^Throwable e]
            (println "default-reporter:" m ":" (.toString e)))))

(defn- default-uncaught-exception-handler []
  (m-do [r (asks :error-reporter)
         c (asks :forkjoin-parallelism-factor)]
        [:return (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ t e] (r (str c t) e)))]))

(defn- init-thread [^Thread t ^Thread$UncaughtExceptionHandler uncaught]
  (doto t
    (.setDaemon true)
    (.setUncaughtExceptionHandler uncaught)))

(def ^:const ^Long number-of-cores (.availableProcessors (Runtime/getRuntime)))

;; eof
