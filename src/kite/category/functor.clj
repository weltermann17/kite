(in-ns 'kite.category)

(defmulti fmap+
          (fn [_ v] (most-general ::functor v)))

(defn fmap [f v]
  (if (satisfies? Functor v) (-fmap v f) (fmap+ f v)))

;; eof
