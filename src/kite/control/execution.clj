(in-ns 'kite.control)

;; mocks and helpers

(defn execute-t
  ([f v] (execute-t (fn [] (f v))))
  ([f] (f)))

(defn execute-all-t [fs v]
  (doseq [f fs] (execute-t f v)))

;; eof
