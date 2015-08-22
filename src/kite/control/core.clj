(in-ns 'kite.control)

(require
  '[clojure.pprint :refer [pprint]]
  '[clojure.string :refer [lower-case]])

;; common helpers

(def pretty pprint)

(defn any?
  "Alias for 'some'."
  [pred coll]
  (when (seq coll)
    (or (pred (first coll)) (recur pred (next coll)))))

(comment pretty)

;; eof
