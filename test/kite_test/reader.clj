(in-ns 'kite-test)

(defprotocol T (a [_]) (b [_]) (c [_]))

(let [ctx {:a 1 :b 2 :c 3}]
  (expect (partial satisfies? Monad) (m-do [a (ask)] [:return a]))
  (println (type (run-reader (m-do [a (ask)] [:return a]))))
  (println ctx)
  (println (
             (run-reader
               (m-do
                 [x (asks :a)]
                 [:return x]))
             ctx))
  )

(let [ctx (reify T (a [_] 1) (b [_] 2) (c [_] 3))]
  (expect (partial satisfies? Monad) (m-do [a (asks a)] [:return (+ a 1)]))
  (println (type (run-reader (m-do [a (asks a)] [:return (+ a 1)]))))
  (println ctx)
  ;  (println ((run-reader (m-do [a (asks a)] [:return (+ a 1)])) ctx))
  )

;; eof
