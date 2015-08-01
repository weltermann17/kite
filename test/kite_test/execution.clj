(in-ns 'kite-test)

(let [config
      {:executor-policy             (reader :forkjoin)
       :forkjoin-parallelism-factor (reader "2.0")
       :error-reporter              (reader (fn [m e] (println "my-own-reporter" (type e) "->" (.toString m))))}
      all (mk-config (default-execution-configuration) config)
      reporter (:error-reporter all)
      handler (:uncaught-exception-handler all)
      default (mk-config (default-execution-configuration) {})
      ]
  (expect 2.0 (:forkjoin-parallelism-factor all))
  (reporter "x" "y")
  (.uncaughtException handler (Thread.) (Exception.))
  (.uncaughtException (:uncaught-exception-handler default) (Thread.) (Exception.))
  (println (:executor all))
  )

;; eof
