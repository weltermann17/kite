(in-ns 'kite-test)

(defprotocol T (a [_]) (b [_]) (c [_]))

(let [ctx {:a 1 :b 2 :c 3}
      m1 (m-do [a (asks :b)] [:return a])
      m2 (m-do [a (ask)] [:return a])
      m3 (m-do [_ (ask)] [:return 2.0])]
  (expect (partial satisfies? Monad) m1)
  (expect (partial satisfies? Monad) m2)
  (expect 2 ((run-reader m1) ctx))
  (expect ctx ((run-reader m2) ctx))
  (expect 2.0 ((run-reader m3) ctx))
  )

(let [ctx (reify T (a [_] 1) (b [_] 2) (c [_] 3))
      m1 (m-do [e (asks b) f (asks c)] [:return [e f]])
      m2 (m-do [e (ask) f (local inc (ask))] [:return (* e f)])
      m3 (m-do [e (asks b) f (asks c) a (ask)] [:return a])]
  (expect (partial satisfies? Monad) m1)
  (expect [2 3] ((run-reader m1) ctx))
  (expect 30 ((run-reader m2) 5))
  (expect ctx ((run-reader m3) ctx))
  )

;; eof
