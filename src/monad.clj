(in-ns 'kite)

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
  ([body type]
   (match [body]
          [([val] :seq)]
          (match val
                 [:return v] `(pure ~type ~v)
                 v v)
          [([fst & rst] :seq)]
          (match fst
                 [:let & vs] `(let [~@vs] ~(m-do* rst type))
                 [:return v] `(>>= (pure ~type ~v) (fn [_#] ~(m-do* rst type)))
                 [:guard v] `(>>= (if ~v (pure ~type nil) (zero ~type))
                                  (fn [_#] ~(m-do* rst type)))
                 [k v] (if type
                         `(>>= ~v (fn [~k] ~(m-do* rst type)))
                         (let [t `t#]
                           `(let [~t ~v]
                              (>>= ~t (fn [~k] ~(m-do* rst t))))))
                 [k v & rs] (m-do* (concat [[k v]] [rs] rst) type)
                 v (if type
                     `(>>= ~v (fn [_#] ~(m-do* rst type)))
                     (let [t `t#]
                       `(let [~t ~v]
                          (>>= ~t (fn [_#] ~(m-do* rst t))))))))))

(defmacro m-do [& body]
  (m-do* body))

(defn lift [f]
  (fn [& m-args] (m-do [args (sequence-a m-args)]
                       [:return (apply f args)])))

(defn join [m]
  (>>= m identity))

(defn >=>
  ([f] f)
  ([f & fs] (fn [& args] (apply >>= (apply f args) fs))))

(comment lift join >=>)

;; eof
