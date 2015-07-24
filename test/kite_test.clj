(ns kite-test
  (:refer-clojure :exclude [await future promise]))

(require '[expectations :refer :all]
         '[kite.category :refer :all]
         '[kite :refer :all])

;;maybe ?
(expect (just 1) (just (- 2 1)))
(expect nothing (nothing))
(expect (just 1) (maybe 1))
(expect nothing (maybe nil))
(expect 2 (maybe 0 inc (maybe 1)))
(expect 0 (maybe 0 inc (maybe nil)))
(expect (just 2) (fmap inc (just 1)))
(expect (nothing) (fmap inc (nothing)))
(expect nothing (fmap inc nothing))
(expect (just 1) (pure (just 2) 1))
(expect (just 1) (pure (nothing) 1))
(expect (just 9) (<*> (just inc) (just 8)))
(expect (just 21) (<*> (just +) (just 8) (just 7) (just 6)))
(expect (just -5) (<*> (just -) (just 8) (just 7) (just 6)))
(expect (nothing) (<*> (just -) (just 8) (just 7) (nothing)))
(expect (just 8) (>>= (just 8) #(just (inc %)) #(just (dec %))))
(expect (just [8 8]) (>>= (just 8) #(just [%1 %1])))
(expect (just [9 9]) (>>= (just 8) #(just (inc %)) #(just [%1 %1])))
(expect (just [8 8]) (sequence-a (repeat 2 (just 8))))
(expect (nothing) (sequence-a [(just 8) (nothing) (just 8)]))
(expect (just 24) ((lift +) (just 8) (just 8) (just 8)))
(expect (just [1 5 3]) (m-do [x (just 1)] (just 2) [:let y 5, z 3] (just [x y z])))
(expect (just [1 5 3]) (m-do [x (just 1)] (just 2) [:let y 5, z 3] [:return [x y z]]))
(expect (just [1 2]) (m-do [x (just 1) y (just 2)] [:return [x y]]))
(expect (nothing) (m-do [x (just 1) y (just 2) z (nothing)] [:return [x y z]]))
(let [half (fn [x] (maybe (when (even? x) (quot x 2))))]
  (expect (just 2) (half 4))
  (expect (just 2) (>>= (just 4) half))
  (expect (just 2) (>>= (just 16) half half half))
  (expect (nothing) (>>= (just 20) half half half)))

;;sequential
(expect [8] (join [[8]]))
(expect [2 3 4] (fmap inc [1 2 3]))
(expect '(2 3 4) (fmap inc '(1 2 3)))
(expect (type [1 2 3 4 5 6 7]) (type (fmap inc [1 2 3])))
(expect [2 4 6 4 5 6] (<*> [(partial * 2) (partial + 3)] [1 2 3]))
(expect [2 4 6 4 5 6 2 3 4] (<*> [(partial * 2) (partial + 3) inc] [1 2 3]))
(expect [2 2 2 4 4 4 6 6 6] (<*> [(partial * 2)] [1 2 3] [1 1 1]))
(expect [] (<*> [(partial * 2)] [1 2 3] [1 1 1] []))

;; try
(expect (type @(->try (/ 2 0))) (type @(->try (/ 1 0))))
(expect ArithmeticException (throw (deref (match-try (->try (/ 1 0)) inc identity))))
(expect ArithmeticException (throw (deref (fmap inc (->try (/ 1 0))))))
(expect (success 2) (->try (+ 1 1)))
(expect (success 2) (fmap inc (->try (+ 1 0))))
(expect (success 8) (<*> (success +) (success 8)))
(expect (success 21) (<*> (success +) (success 8) (success 7) (success 6)))
(expect (success 21) (<*> (success +) (just 8) (just 7) (just 6)))
(expect (nothing) (<*> (success +) (just 8) (nothing) (just 6)))
(expect (failure 7) (<*> (success +) (success 8) (failure 7) (success 6)))
(expect (failure 7) (<*> (success +) (success 8) (failure 7) (failure 6)))
(expect (success 8) (>>= (success 8) #(success (inc %)) #(success (dec %))))
(expect (success [8 8]) (>>= (success 8) #(success [%1 %1])))
(expect (success [9 9]) (>>= (success 8) #(success (inc %)) #(success [%1 %1])))
(expect (success [8 8]) (sequence-a (repeat 2 (success 8))))
(expect (failure 8) (sequence-a [(success 8) (failure 8) (success 8)]))
(expect (success 24) ((lift +) (success 8) (success 8) (success 8)))
(expect (success [1 5 3]) (m-do [x (success 1)] (success 2) [:let y 5, z 3] (success [x y z])))
(expect (success [1 2]) (m-do [x (success 1) y (success 2)] [:return [x y]]))
(expect (failure 3) (m-do [x (success 1) y (success 2) z (failure 3)] [:return [x y z]]))
(expect (nothing) (m-do [x (success 1) y (nothing) z (success 3)] [:return [x y z]]))
(expect (success [1 2 3]) (m-do [x (success 1) y (->try 2) z (just 3)] [:return [x y z]]))

;;future
(expect (success 2) (await (future (+ 1 1)) 100))
(expect (partial satisfies? Failure) (await (future (/ 1 0)) 100))
(expect (success 3) (await (fmap (lift inc) (future (+ 1 1))) 100))
(expect (success 6)
        (await (m-do [x (immediate 1)
                      y (future (+ 2 0))
                      z (immediate 3)]
                     [:return (+ x y z)]) 100))
(expect (partial satisfies? Failure)
        (await (m-do [x (immediate 1)
                      y (future (/ 2 0))
                      z (immediate 3)]
                     [:return (+ x y z)]) 100))

;(expect (success 9) (await (<*> (future inc) (future (+ 8 0))) 100))
;(expect (success 8) (await (<*> (future +) (future 8)) 100))
;(expect (success 21) (await (<*> (lift +) (future 8) (future 7) (future 6)) 100))

;; eof
