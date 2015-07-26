(in-ns 'kite.control)

(defn equal? [a b p v]
  "True if a and b are identical or b satisfies p and v returns true."
  (or
    (identical? a b)
    (and
      (satisfies? p b)
      (v))))

(defn p [a] (if a (.toString a) a))

;; eof
