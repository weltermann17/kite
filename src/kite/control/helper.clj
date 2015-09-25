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

(defmacro ignore [& body]
  "Use this if it is save to ignore any exception thrown by 'body'."
  `(try ~@body (catch Throwable _#)))

(def ^:constant empty-byte-array ^bytes (byte-array 0))

;; eof
