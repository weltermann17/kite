(in-ns 'kite)

;;   functor
;;     |
;;   applicative
;;     |
;;   monad

(derive ::applicative ::functor)
(derive ::monad ::applicative)

(def monad (cons [::monad #'Monad] []))
(def applicative (cons [::applicative #'Applicative] monad))
(def functor (cons [::functor #'Functor] applicative))

(def ^:private hierarchy (hash-map
                           ::monad monad
                           ::applicative applicative
                           ::functor functor))

(defn most-general [t inst]
  (some (fn [[t p]] (when (satisfies? (deref p) inst) t)) (t hierarchy)))

;; eof
