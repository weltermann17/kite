(in-ns 'kite.control)

;; mocks and helpers

(defn mock-execute
  ([f v] (mock-execute (fn [] (f v))))
  ([f] (f)))

(defn mock-execute-all [fs v]
  (doseq [f fs] (mock-execute f v)))

;; eof
