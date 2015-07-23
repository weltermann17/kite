(ns kite-test
  (:refer-clojure :exclude [await future promise]))

(require '[expectations :refer :all]
         '[kite :refer :all])

;; maybe
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
(expect [8] (join [[8]]))
(expect (just [1 5 3]) (m-do [x (just 1)] (just 2) [:let y 5, z 3] (just [x y z])))
(expect (just [1 5 3]) (m-do [x (just 1)] (just 2) [:let y 5, z 3] [:return [x y z]]))
(expect (just [1 2]) (m-do [x (just 1) y (just 2)] [:return [x y]]))
(expect (nothing) (m-do [x (just 1) y (just 2) z (nothing)] [:return [x y z]]))

;;sequential
(expect [2 3 4] (fmap inc [1 2 3]))
(expect '(2 3 4) (fmap inc '(1 2 3)))
(expect (type [1 2 3 4 5 6 7]) (type (fmap inc [1 2 3])))

;; eof
