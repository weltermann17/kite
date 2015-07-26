(in-ns 'kite)

(import
  [clojure.lang AFunction])

(extend-type AFunction
  Category
  (-id [_] identity)
  (-comp [a b] (comp a b))

  Pure
  (-pure [_ a] (fn [_] a)))

;; eof
