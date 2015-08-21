(in-ns 'kite-test)

(comment
  (import
    (java.util.concurrent TimeoutException)))

(let [ctx {}
      config {}
      env (add-executor-context ctx config)]
  (expect (partial satisfies? Promise) (promise))
  (expect (partial satisfies? Reader) (promise))
  (expect (partial satisfies? Reader) (complete (promise) 7))
  ;(expect nil (run-reader (complete (complete (promise) 7) 8) env)
  )

(comment (let [p (promise)
               f (->future p)
               c (m-do [env (ask)]
                       [:let _ (run-reader (on-complete f (fn [a] (println "on-complete" a))) env)]
                       [:return (run-reader (complete p 7) env)])]
           (expect nil (run-reader c env)))
         (expect (partial satisfies? Future) (immediate 17))
         (expect (success 17) (run-reader (m-do [x (immediate 10)
                                                 y (immediate 7)]
                                                [:return (+ x y)]) env)))


(comment (expect (partial satisfies? Future) (->future ((promise) env)))
         (expect (partial satisfies? Failure) (await (->future ((promise) env)) 1))
         (expect (partial satisfies? Future) ((immediate 17) env))
         (expect (partial ifn?) (immediate 17))
         (expect (success 17) (await ((immediate 17) env) 10))
         (comment (expect (success 2)
                          (await (m-do [x ((immediate 1) env)
                                        ]
                                       ;y ((immediate (+ 2 0)) env)
                                       ;z ((immediate 3) env)]
                                       [:return (+ x 1)]) 10)))


         (let [ctx {}
               config {}
               env (add-executor-context ctx config)
               m (fn [] (partial run-reader (m-do [e (ask)] [:return (await ((immediate 17) e) 1)])))]
           (expect (success 17) ((m) env))
           ))

(comment
  (expect (success 2) (await (future (+ 1 1)) 100))
  (expect (partial satisfies? Failure) (await (future (/ 1 0)) 100))
  (expect (success 3) (await (fmap (lift inc) (future (+ 1 1))) 100))
  (expect (success 6)
          (await (m-do [x (immediate 1)
                        y (just (+ 2 0))
                        z (right 3)]
                       [:return (+ x y z)]) 100))
  (expect (partial satisfies? Failure)
          (await (m-do [x (immediate 1)
                        y (future (/ 2 0))
                        z (immediate 3)]
                       [:return (+ x y z)]) 100))
  (expect (partial satisfies? Failure)
          (await (m-do [x (immediate 1)
                        y (future (/ 2 0))
                        z (left 3)]
                       [:return (+ x y z)]) 100))
  (expect (success 9) (await (>>= (future 8) #(future (inc %))) 100))
  (expect (success 8) (await (>>= (future 8) #(immediate (inc %)) #(future (dec %))) 100))
  (expect (success 8) (await (>>= (future 8) #(future (inc %)) #(future (dec %))) 100))
  (expect (success [8 8]) (await (>>= (future 8) #(future [%1 %1])) 100))
  (expect (future [9 9]) (>>= (future 8) #(future (inc %)) #(future [%1 %1])))
  (expect (success 9) (await (<*> (future inc) (future 8)) 100))
  (expect (future 9) (<*> (future inc) (future 8)))
  (expect (future 9) (<*> (future inc) (immediate 8)))
  (expect (future 9) (<*> (future inc) (success 8)))
  (expect (future 9) (<*> (future inc) (right 8)))
  (expect (future 9) (<*> (future inc) (just 8)))
  ;(expect-focused (success 15) (await (<*> (future +) (future 8) (future 7)) 100))
  ;(expect (success 21) (await (<*> (lift +) (future 8) (future 7) (future 6)) 100))
  (let [fut (fn [w i] (future (Thread/sleep w) i))
        fut-fail (fn [w i] (future (Thread/sleep w) (/ i 0)))
        f1 (first-result (fut 30 1) (fut 20 2) (fut 10 3))
        f2 (first-success (fut 30 1) (fut 20 2) (fut 10 3))
        f3 (first-result (fut-fail 30 1) (fut 20 2) (fut 10 3))
        f4 (first-success (fut-fail 30 1) (fut-fail 20 2) (fut 10 3))
        f5 (first-success (fut-fail 30 1) (fut-fail 20 2) (fut-fail 10 3))
        ]
    (expect (success 1) (await f1 100))                     ;; should be 3
    (expect (success 1) (await f2 100))                     ;; should be 3
    (expect (partial satisfies? Failure) (await f3 100))    ;; should be 3
    (expect (success 3) (await f4 100))
    (expect (partial satisfies? Failure) (await f5 100))
    ))

;; eof
