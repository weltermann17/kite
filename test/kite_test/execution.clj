(in-ns 'kite-test)

(import
  (java.util.concurrent
    ThreadPoolExecutor$AbortPolicy))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(let [config
      {
       ;:executor-policy             (reader :threadpoolXX)
       :forkjoin-parallelism-factor (reader 2.0)
       ; :forkjoin-parallelism        (reader 12)
       :threadpool-minimum-size     (reader 200)
       :threadpool-maximum-size     (reader (+ 200 1))
       :threadpool-rejection-policy (reader (ThreadPoolExecutor$AbortPolicy.))
       :forkjoin-error-reporter     (reader (fn [m e] (println "my-own-reporter" e "<-" m)))
       }
      all (mk-config (default-execution-configuration) config)
      reporter (:forkjoin-error-reporter all)
      ;handler (:forkjoin-uncaught-exception-handler all)
      ;default (mk-config (default-execution-configuration) {})
      ctx (add-executor-context (->Context {}) config)
      ; proto (mk-protocol config)
      ]
  (expect 2.0 (:forkjoin-parallelism-factor all))
  ; (reporter "x" "y")
  ;(.uncaughtException handler (Thread.) (Exception. "test"))
  ;(.uncaughtException (:forkjoin-uncaught-exception-handler default) (Thread.) (Exception.))
  ; (println (:executor all))
  (println (executor ctx))
  (with-progress-reporting (quick-bench (executor ctx)))
  ; (with-progress-reporting (quick-bench (:executor all)))
  ;(with-progress-reporting (quick-bench (executor2 proto)))
  )

;; eof
