(in-ns 'kite-test)

(expect [8] (join [[8]]))
(expect [1] (pure [] 1))
(expect [2 3 4] (fmap inc [1 2 3]))
(expect '(2 3 4) (fmap inc '(1 2 3)))
(expect (type [1 2 3 4 5 6 7]) (type (fmap inc [1 2 3])))
(expect [2 4 6 4 5 6] (<*> [(partial * 2) (partial + 3)] [1 2 3]))
(expect [2 4 6 4 5 6 2 3 4] (<*> [(partial * 2) (partial + 3) inc] [1 2 3]))
(expect [2 2 2 4 4 4 6 6 6] (<*> [(partial * 2)] [1 2 3] [1 1 1]))
(expect [] (<*> [(partial * 2)] [1 2 3] [1 1 1] []))

;; eof
