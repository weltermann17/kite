(in-ns 'kite)

;;   functor
;;     |
;;   applicative
;;     |
;;   monad
;;     |
;;   monoid
;;     |
;;   monoidsum

(derive ::applicative ::functor)
(derive ::monad ::applicative)

(def monoidsum (cons [::monoidsum #'MonoidSum] []))
(def monoid (cons [::monoid #'Monoid] []))
(def monad (cons [::monad #'Monad] []))
(def applicative (cons [::applicative #'Applicative] monad))
(def functor (cons [::functor #'Functor] applicative))

(def ^:private hierarchy (hash-map
                           ::monoidsum monoidsum
                           ::monoid monoid
                           ::monad monad
                           ::applicative applicative
                           ::functor functor))

(defn most-general [t inst]
  (some (fn [[t p]] (when (satisfies? (deref p) inst) t)) (t hierarchy)))

;; eof
