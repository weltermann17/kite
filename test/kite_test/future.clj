(in-ns 'kite-test)

(import
  java.util.concurrent.TimeoutException)

;; shortcuts

(def ctx (add-execution-context {} {}))

(defmacro ! [body] `(with-context ctx (await ~body 10)))

;; tests

(expect (success 2) (! (future (+ 1 1))))
(expect (success 2) (! (future (+ 1 1))))
(expect success? (! (future (+ 1 1))))
(expect failure? (! (future (/ 1 0))))
(expect failure? (! (future (do (Thread/sleep 100) (/ 1 1)))))
(expect TimeoutException @(! (future (do (Thread/sleep 100) (/ 1 1)))))
(expect TimeoutException @(! (future (Thread/sleep 100) (/ 1 1))))
(expect failure? (! (future (/ 1 0))))
(expect (success 3) (! (fmap (lift inc) (future (+ 1 1)))))
(expect (success 6)
        (! (m-do [x (immediate 1)
                  y (just (+ 2 0))
                  z (right 3)]
                 [:return (+ x y z)])))
(expect ArithmeticException
        @(! (m-do [x (immediate 1)
                   y (future (/ 2 0))
                   z (immediate 3)]
                  [:return (+ x y z)])))
(expect (success 10)
        (! (m-do [x (future (+ 1 1))
                  y (future (/ 4 2))
                  z (future (* 2 3))]
                 [:return (+ x y z)])))
(expect ArithmeticException
        @(! (m-do [x (immediate 1)
                   y (future (/ 2 0))
                   z (left 3)]
                  [:return (+ x y z)])))
(expect (success 9) (! (>>= (future 8) #(future (inc %)))))
(expect (success 8) (! (>>= (future 8) #(immediate (inc %)) #(future (dec %)))))
(expect (success 8) (! (>>= (future 8) #(future (inc %)) #(future (dec %)))))
(expect (success [8 8]) (! (>>= (future 8) #(future [%1 %1]))))
(expect (success [9 9]) (! (>>= (future 8) #(future (inc %)) #(future [%1 %1]))))
(expect (success 9) (! (<*> (future inc) (future 8))))
(expect (success 9) (! (<*> (future inc) (future 8))))
(expect (success 9) (! (<*> (future inc) (immediate 8))))
(expect (success 9) (! (<*> (future inc) (success 8))))
(expect (success 9) (! (<*> (future inc) (right 8))))
(expect (with-context ctx (future 9)) (with-context ctx (<*> (future inc) (just 8))))
(with-context ctx
  (let [fut (fn [w i] (future (Thread/sleep w) i))
        fut-fail (fn [w i] (future (Thread/sleep w) (/ i 0)))
        f1 (first-result (fut 30 1) (fut 20 2) (fut 10 3))
        f2 (first-success (fut 30 1) (fut 20 2) (fut 10 3))
        f3 (first-result (fut-fail 30 1) (fut 20 2) (fut 10 3))
        f4 (first-success (fut-fail 30 1) (fut-fail 20 2) (fut 10 3))
        f5 (first-success (fut-fail 30 1) (fut-fail 20 2) (fut-fail 10 3))
        ]
    (expect (success 3) (! f1))
    (expect (success 3) (! f2))
    (expect (success 3) (! f3))
    (expect (success 3) (! f4))
    (expect failure? (! f5))
    ))
(comment (expect (success 9) (! (let [f (future (+ 8 1))]
                                  (on-success f #(expect 9 %))
                                  (on-failure f #(expect ArithmeticException %)) f)))
         (expect failure? (! (let [f (future (/ 8 0))]
                               (on-success f #(expect 9 %))
                               (on-failure f #(expect ArithmeticException %)) f)))
         )

;; eof
