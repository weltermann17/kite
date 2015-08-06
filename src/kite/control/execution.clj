(in-ns 'kite.control)

;; mocks and helpers

(defn mock-executeX
  ([f v] (mock-executeX (fn [] (f v))))
  ([f] (f)))

(defn mock-execute-allX [fs v]
  (doseq [f fs] (mock-executeX f v)))

;; eof
