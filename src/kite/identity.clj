(in-ns 'kite)

(def identitymonad
  (reify
    Object
    (toString [_] "Identity")
    (equals [this o] (identical? this o))

    Functor
    (-fmap [m _] m)

    Pure
    (-pure [_ _] identity)

    Applicative
    (-apply [m _] m)

    Monad
    (-bind [m f] (f m))))

(comment identitymonad)

;; eof
