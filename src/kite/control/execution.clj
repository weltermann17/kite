(in-ns 'kite.control)

;; mocks and helpers
;; TODO: move to the right places

(defn execute
  ([f v] (execute (fn [] (f v))))
  ([f] (f)))

(defn execute-all [fs v]
  (doseq [f fs] (execute f v)))

;; eof
