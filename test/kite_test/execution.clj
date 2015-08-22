(in-ns 'kite-test)

(import
  (java.util.concurrent
    ;ForkJoinPool
    ThreadPoolExecutor$AbortPolicy))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(let [config {
              :executor-policy             :threadpool
              :scheduler-minimum-size      16
              :forkjoin-parallelism-ratio  3.0
              :forkjoin-parallelism        12
              :threadpool-minimum-size     200
              :a-false-value               false
              :threadpool-maximum-size     (m-do [mn (asks :threadpool-minimum-size)]
                                                 [:return (* mn 2)])
              :threadpool-rejection-policy (ThreadPoolExecutor$AbortPolicy.)
              :forkjoin-error-reporter     (fn [m e] (println "my-own-reporter" e "<-" m))
              }
      ctx1 (add-executor-context {} config)
      ctx2 (add-executor-context {} {:forkjoin-parallelism 16})
      cfg1 (:config ctx1)
      ;e1 (run-reader (execute (fn [] (Thread/sleep 10) (println "Hi thread!"))) ctx1)
      ;e2 (run-reader (execute (fn [] (Thread/sleep 100) (println "Hi fork!"))) ctx2)
      ;e3 (run-reader (execute (fn [a] (Thread/sleep 100) (println (str "Hi fork!" a))) 7) ctx2)
      ; e4 ((fmap run-reader (execute-all [(fn [a] (println "Hi1!" a)) (fn [a] (println "Hi2!" a))] 3)))
      ;fs [(execute (fn [] (Thread/sleep 10) (str "Hi seq1!")))
      ;    (execute (fn [] (Thread/sleep 50) (str "Hi seq2!")))]
      ;fns [(fn [a] (Thread/sleep 10) (str "Hi seq11!" a))
      ;     (fn [a] (Thread/sleep 50) (str "Hi seq22!" a))]
      ; e5 (fmap #((run-reader %) ctx1) fs)
      ;e6 (m-do [r (ask)] [:return (fmap #((run-reader %) r) fs)])
      ;e6 (fn [fss v] (m-do [r (ask)] [:let fss (fmap #(execute % v) fns)] [:return (doseq [f fss] (run-reader f r))]))
      ;e7 (run-reader (e6 fns 3) ctx1)
      ;_ (Thread/sleep 100)
      ;e8 (run-reader (execute-all fns 4) ctx2)
      ;@forkj (:executor ctx2)
      ]
  (expect 200 (:threadpool-minimum-size cfg1))
  (expect 400 (:threadpool-maximum-size cfg1))
  (expect 200 @(config-int :threadpool-minimum-size cfg1))
  (expect 200 @(config-long :threadpool-minimum-size cfg1))
  (expect 200.0 @(config-double :threadpool-minimum-size cfg1))
  (expect 200 @(config-num :threadpool-minimum-size cfg1))
  (expect (bigdec 200) @(config-bigdec :threadpool-minimum-size cfg1))
  (expect "200" @(config-str :threadpool-minimum-size cfg1))
  (expect (partial satisfies? Failure) (config-boolean :XYZ cfg1))
  (expect (partial satisfies? Failure) (config-int :XYZ cfg1))
  (expect IndexOutOfBoundsException @(config-int :XYZ cfg1))
  (expect (success false) (config-boolean :a-false-value cfg1))
  ;(expect nil (with-context ctx1 (execute (fn [] (println "fn" 7 (Thread/currentThread))))))
  (expect nil
    (dorun
      (for [i (range 1000)]
        (with-context (merge ctx2 {:i i})
          (execute (fn []
                     (let [j (from-context :i)]
                       (if (not= i j)
                         (println "not" i j)
                         (when (= 0 (mod i 17))
                           ;(dorun (for [x (range 1000) y (range 1 100)] (/ x y)))
                           ;(Thread/sleep 500)
                           ;(println i j)
                           )))))))))
  (expect nil (Thread/sleep 200))
  ;(expect nil (println (:executor ctx2)))

  ;(expect (partial reader?) (execute-all [(fn [] nil) (fn [] nil)] nil))

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
