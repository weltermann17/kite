(in-ns 'kite-test)

(let [config
      {
       :executor-policy             (reader :threadpool)
       :forkjoin-parallelism-factor (reader 2.0)
       :forkjoin-parallelism        (reader 12)
       :threadpool-minimum-size     (reader 200)
       :threadpool-maximum-size     (reader (+ 200 1))
       :forkjoin-error-reporter     (reader (fn [m e] (println "my-own-reporter" e "<-" m)))
       }
      all (mk-config (default-execution-configuration) config)
      reporter (:forkjoin-error-reporter all)
      handler (:forkjoin-uncaught-exception-handler all)
      default (mk-config (default-execution-configuration) {})
      ]
  (expect 2.0 (:forkjoin-parallelism-factor all))
  (reporter "x" "y")
  ;(.uncaughtException handler (Thread.) (Exception. "test"))
  ;(.uncaughtException (:forkjoin-uncaught-exception-handler default) (Thread.) (Exception.))
  (println (:executor all))
  )

;; eof
