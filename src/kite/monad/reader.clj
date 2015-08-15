(in-ns 'kite.monad)

(defprotocol Reader
  (run-reader [f e]))

(defn- mk-reader [f]
  (reify
    Reader
    (run-reader [_ e] (f e))

    Functor
    (-fmap [_ k] (k f))

    Pure
    (-pure [_ k] (mk-reader (constantly k)))

    Monad
    (-bind [_ k] (mk-reader (fn [g] (run-reader (k (f g)) g))))))

;; fn

(defn reader? [e] (satisfies? Reader e))

(defn ask []
  (mk-reader (fn [e] (identity e))))

(defn asks [f]
  (mk-reader (fn [e] (f e))))

(defn local [f g]
  (mk-reader (fn [e] (run-reader g (f e)))))

;; macro

(defmacro reader [body]
  `(m-do [_# (ask)] [:return ~body]))

;; Reader monad transformer

(defn- reader-t [f inner]
  (reify
    Reader
    (run-reader [_ e] (f e))

    Functor
    (-fmap [_ k] (k f))

    Pure
    (-pure [_ k] (mk-reader (constantly k)))

    Monad
    (-bind [_ k] (mk-reader (fn [g] (run-reader (k (f g)) g))))

    MonadTransformer
    (-inner [_] inner)
    (-lift [_ _] nil)))

(comment reader-t)

;; eof
