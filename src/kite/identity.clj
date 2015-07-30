(in-ns 'kite)

(def identity-monad
  "Why do we actually have this?"
  (reify
    Object
    (toString [_] "IdentityMonad")
    (equals [this o] (identical? this o))

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
