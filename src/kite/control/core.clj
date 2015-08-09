(in-ns 'kite.control)

(require
  '[clojure.pprint :refer [pprint]])

;; common helpers

(def pretty-print pprint)

(defn any?
  "Alias for 'some'."
  {:static true}
  [pred coll]
  (when (seq coll)
    (or (pred (first coll)) (recur pred (next coll)))))

;; eof
