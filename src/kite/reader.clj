(in-ns 'kite)

(defprotocol Reader
  (run-reader [env]))

(defn reader [r]
  "This is probably the most difficult one, for a Scala guy that is."
  (reify
    Reader
    (run-reader [_] r)

    Object
    (equals [this o] (equal? this o Reader #(= r (run-reader o))))
    (hashCode [_] (hash r))
    (toString [_] (str "Reader " r))

    Functor
    (-fmap [_ f] (f r))

    Pure
    (-pure [_ u] (reader (fn [_] u)))

    Monad
    (-bind [_ k] (reader (fn [g] ((run-reader (k (r g))) g))))))

(defn ask [] (reader (fn [] identity)))

(defn asks [f] (reader (fn [env] (f env))))

(defn local [f g] (reader (fn [r] (g (f r)))))

;; eof
