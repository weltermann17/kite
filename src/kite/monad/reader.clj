(in-ns 'kite.monad)

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
    (-bind [_ f] (reader (fn [k] ((run-reader (f (r k))) k))))))

(defn ask [] (reader (fn [r] (identity r))))

(defn asks [f] (reader (fn [r] (f r))))

(defn local [f g] (reader (fn [r] ((run-reader g) (f r)))))

;; macro

(defmacro reader-m [& body]
  `(m-do [e# (ask)]
         [:return ~@body]))

;; eof
