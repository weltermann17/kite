(in-ns 'kite.category)

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

(def ^:private hierarchy (array-map
                           ::monoidsum monoidsum
                           ::monoid monoid
                           ::monad monad
                           ::applicative applicative
                           ::functor functor))

(defn most-general [tp inst]
  (any?
    (fn [[t p]] (when (satisfies? (deref p) inst) t))
    (tp hierarchy)))

;; eof
