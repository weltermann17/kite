(in-ns 'kite.monad)

(def identity-monad
  "Why do we actually have this?"
  (reify
    Object
    (equals [this o] (identical? this o))
    (toString [_] "Identity")

    Functor
    (-fmap [m _] m)

    Pure
    (-pure [_ _] identity)

    Applicative
    (-apply [m _] m)

    Monad
    (-bind [m f] (f m))))

(comment identity-monad)

;; eof
