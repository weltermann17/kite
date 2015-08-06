(in-ns 'kite-test)

(import
  (java.util.concurrent
    ForkJoinPool
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
      ;e1 ((run-reader (execute (fn [] (Thread/sleep 10) (println "Hi thread!")))) ctx1)
      ;e2 ((run-reader (execute (fn [] (Thread/sleep 100) (println "Hi fork!")))) ctx2)
      e3 ((run-reader (execute (fn [a] (Thread/sleep 100) (println "Hi fork!" a)) 7)) ctx2)
      ;e4 ((fmap run-reader (execute-all [(fn [a] (println "Hi1!" a)) (fn [a] (println "Hi2!" a))] 3)))
      fs [(execute (fn [] (Thread/sleep 10) (println "Hi seq1!")))
          (execute (fn [] (Thread/sleep 50) (println "Hi seq2!")))]
      fns [(fn [a] (Thread/sleep 10) (println "Hi seq11!" a))
           (fn [a] (Thread/sleep 50) (println "Hi seq22!" a))]
      ; e5 (fmap #((run-reader %) ctx1) fs)
      ;e6 (m-do [r (ask)] [:return (fmap #((run-reader %) r) fs)])
      e6 (fn [fss v] (m-do [r (ask)] [:let fss (fmap #(execute % v) fns)] [:return (doseq [f fss] ((run-reader f) r))]))
      e7 ((run-reader (e6 fns 3)) ctx1)
      _ (Thread/sleep 100)
      e8 ((run-reader (execute-all fns 4)) ctx2)
      forkj (:executor ctx2)
      ]
  ;(println e1 e2 e3)
  ; (Thread/sleep 500)
  ; (pretty-print ctx1)
  ; (pretty-print ctx2)
  ; (with-progress-reporting (quick-bench (:executor ctx1)))
  ; (with-progress-reporting (quick-bench (:executor (mk-config (default-execution-configuration) config))))
  ; (with-progress-reporting (quick-bench ((run-reader (execute (fn [] 1))) ctx1)))
  ; (with-progress-reporting (quick-bench ((run-reader (execute (fn [a] a) 3)) ctx2)))
  ; (let [fs [(fn [a] a)]]
  ;  (with-progress-reporting (quick-bench ((run-reader (execute-all fs 3)) ctx2))))
  ; (with-progress-reporting (quick-bench (.execute ^ForkJoinPool forkj ^Runnable (fn [] 2))))

  )

;; eof
