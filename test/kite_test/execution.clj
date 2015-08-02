(in-ns 'kite-test)

(import
  (java.util.concurrent
    ThreadPoolExecutor$AbortPolicy))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(let [config
      {
       :executor-policy             (reader :threadpool)
       :scheduler-minimum-size      (reader 16)
       :forkjoin-parallelism-factor (reader 3.0)
       :forkjoin-parallelism        (reader 12)
       :threadpool-minimum-size     (reader 200)
       :threadpool-maximum-size     (m-do [mn (asks :threadpool-minimum-size)] [:return (* mn 2)])
       :threadpool-rejection-policy (reader (ThreadPoolExecutor$AbortPolicy.))
       :forkjoin-error-reporter     (reader (fn [m e] (println "my-own-reporter" e "<-" m)))
       }
      ctx1 (add-executor-context {} config)
      ctx2 (add-executor-context {} {})
      e1 ((run-reader (execute (fn [] (Thread/sleep 10) (println "Hi thread!")))) ctx1)
      e2 ((run-reader (execute (fn [] (Thread/sleep 100) (println "Hi fork!")))) ctx2)
      ]
  ; (println e1 e2)
  ; (Thread/sleep 500)
  ; (pretty-print ctx1)
  ; (with-progress-reporting (quick-bench (:executor ctx1)))
  ; (with-progress-reporting (quick-bench (:executor (mk-config (default-execution-configuration) config))))
  (with-progress-reporting (quick-bench ((run-reader (execute (fn [] 1))) ctx1)))
  (with-progress-reporting (quick-bench ((run-reader (execute (fn [] 2))) ctx2)))

  )

;; eof
