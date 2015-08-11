(in-ns 'kite.category)

(defmulti pure+
  (fn [a _] (most-general ::applicative a)))

(defn pure [m a]
  (if (satisfies? Pure m)
    (-pure m a)
    (pure+ m a)))

(defmulti <*>+
  (fn [af & _] (most-general ::applicative af)))

(declare <*)

(defn <*>
  ([af] (fmap #(%) af))
  ([af av & avs]
   {:pre [af]}
   (cond
     avs (apply <*> (<* af av) avs)
     (satisfies? Applicative af) (-apply af av)
     :else (<*>+ af av))))

(defn <*
  ([af] af)
  ([af a & r]
   (if r
     (apply <* (<* af a) r)
     (<*> (fmap (fn [f] #(partial f %)) af) a))))

(defn sequence-a [[a & as]]
  "Need more examples for this one."
  (apply <*> (pure a vector) a as))

(defmethod fmap+ ::applicative [f v]
  (<*> (pure v f) v))

;; eof
