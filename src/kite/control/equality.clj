(in-ns 'kite.control)

(defn test-eq [a b p v]
  "True if a and b are identical or b satisfies p and v returns true."
  (or
    (identical? a b)
    (and
      (satisfies? p b)
      (v))))

;; eof
