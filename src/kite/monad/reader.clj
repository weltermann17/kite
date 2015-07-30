(in-ns 'kite.monad)

(defprotocol Reader
  (run-reader [env]))

(defn- ^Reader mk-reader [r]
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
    (-pure [_ u] (mk-reader (fn [_] u)))

    Monad
    (-bind [_ f] (mk-reader (fn [k] ((run-reader (f (r k))) k))))))

(defn ^Reader ask [] (mk-reader (fn [r] (identity r))))

(defn ^Reader asks [f] (mk-reader (fn [r] (f r))))

(defn ^Reader local [f g] (mk-reader (fn [r] ((run-reader g) (f r)))))

;; macro

(defmacro ^Reader reader [& body]
  `(m-do [_# (ask)]
         [:return ~@body]))

;; eof
