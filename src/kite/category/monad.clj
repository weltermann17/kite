(in-ns 'kite.category)

(require
  '[clojure.core.match :refer [match]])

(defmulti >>=+
          (fn [m _] (most-general ::monad m)))

(defn >>=
  ([m] m)
  ([m f & fs] (cond
                fs (apply >>= (>>= m f) fs)
                (satisfies? Monad m) (-bind m f)
                :else (>>=+ m f))))

(defmethod <*>+ ::monad [af av]
  (>>= af (fn [f] (>>= av (fn [v] (pure af (f v)))))))

(defn m-do*
  ([body] (m-do* body false))
  ([body t]
   (match [body]
          [([val] :seq)]
          (match val
                 [:return v] `(pure ~t ~v)
                 v v)
          [([fst & rst] :seq)]
          (match fst
                 [:let & vs] `(let [~@vs] ~(m-do* rst t))
                 [:return v] `(>>= (pure ~t ~v) (fn [_#] ~(m-do* rst t)))
                 [:guard v] `(>>= (if ~v (pure ~t nil) (zero ~t))
                                  (fn [_#] ~(m-do* rst t)))
                 [k v] (if t
                         `(>>= ~v (fn [~k] ~(m-do* rst t)))
                         (let [t `t#]
                           `(let [~t ~v]
                              (>>= ~t (fn [~k] ~(m-do* rst t))))))
                 [k v & rs] (m-do* (concat [[k v]] [rs] rst) t)
                 v (if t
                     `(>>= ~v (fn [_#] ~(m-do* rst t)))
                     (let [t `t#]
                       `(let [~t ~v]
                          (>>= ~t (fn [_#] ~(m-do* rst t))))))))))

(defmacro m-do [& body]
  (m-do* body))

(defn lift-without-stack-overflow [f]
  (fn [ma mb] (m-do [a ma
                     b mb]
                    [(pure ma (f a b))])))

(defn lift [f]
  (fn [& m-args] (m-do [args (sequence-a m-args)]
                       [:return (apply f args)])))

(defn join [m]
  (>>= m identity))

(defn >=>
  ([f] f)
  ([f & fs] (fn [& args] (apply >>= (apply f args) fs))))

;; eof
