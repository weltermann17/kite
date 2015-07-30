(in-ns 'kite-test)

(let [config
      {:error-reporter
       (reader (fn [m e] (println "my-own-reporter" (type e) "->" (.toString m))))}
      all (mk-config config)
      reporter (:error-reporter all)
      handler (:uncaught-exception-handler all)
      default (mk-config {})
      ]
  (expect 2.0 (:forkjoin-parallelism-factor all))
  (reporter "x" "y")
  (.uncaughtException handler (Thread.) (Exception.))
  (.uncaughtException (:uncaught-exception-handler default) (Thread.) (Exception.))
  ((:thread-factory default) "Hi!")
  )

;; eof
