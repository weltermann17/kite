(in-ns 'kite-test)

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
(expect (success 8) (await (>>= (immediate 8) #(future (inc %)) #(future (dec %))) 100))
(expect (success 8) (await (>>= (immediate 8) #(future (inc %)) #(future (dec %))) 100))
(expect (success [8 8]) (await (>>= (future 8) #(future [%1 %1])) 100))
(expect (success [9 9]) (await (>>= (immediate 8) #(future (inc %)) #(future [%1 %1])) 100))
;(expect nil? (sequence-a (repeat 1 (future-fn (+ 8 0)))))
;(expect (failure 8) (await (sequence-a [(immediate 8) (future (failure 8)) (immediate 8)]) 100))

;(expect (success 9) (await (<*> (future inc) (future (+ 8 0))) 100))
;(expect (success 8) (await (<*> (future +) (future 8)) 100))
;(expect (success 21) (await (<*> (lift +) (future 8) (future 7) (future 6)) 100))

;; eof
