(in-ns 'kite.control)

;; mocks and helpers

(defn execute
  ([f v] (execute (fn [] (f v))))
  ([f] (f)))

(defn execute-all [fs v]
  (doseq [f fs] (execute f v)))

;; eof
