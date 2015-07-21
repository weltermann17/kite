(in-ns 'kite)

(defprotocol Reader)

(defn reader [v]
  (reify

    Reader

    Object
    (equals [_ o] (and (satisfies? Reader o) (= v (value o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Reader " v))

    Functor
    (-fmap [_ f] (reader (f v)))

    Pure
    (-pure [_ u] (fn [_] u))

    Applicative
    (-ap [_ _] ::nyi)

    Monad
    (-bind [m f] (fn [r] ((f (m r)) r)))))

(defn ask [] identity)

(defn asks [f] (fn [r] (f r)))

(defn local [f g] (fn [r] (g (f r))))

(comment ask asks local reader)

;; eof
