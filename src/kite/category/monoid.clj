(in-ns 'kite.category)

(defn zero [m]
  (-zero m))

(defn plus [a & as]
  (cond
    (satisfies? MonoidSum a) (-sum a as)
    (satisfies? Monoid a) (reduce -plus a as)
    :else (throw (UnsupportedOperationException. "Monoid/-plus"))))

(comment plus)

;; eof
