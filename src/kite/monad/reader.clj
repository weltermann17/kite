(in-ns 'kite.monad)

(defprotocol Reader
  (run-reader [env]))

(defn- ->reader [r]
  "This is probably the most difficult one, for a Scala guy that is. Ackn."
  (reify
    Reader
    (run-reader [_] r)

    Functor
    (-fmap [_ f] (f r))

    Pure
    (-pure [_ u] (->reader (fn [_] u)))

    Applicative
    (-apply [m _] (prn "suprise! applicative" m) m)

    Monad
    (-bind [_ f] (->reader (fn [k] ((run-reader (f (r k))) k))))))

;; fn

(defn ask [] (->reader (fn [r] (identity r))))

(defn asks [f]
  (->reader (fn [r] (f r))))

(defn local [f g] (->reader (fn [r] ((run-reader g) (f r)))))

;; macro

(defmacro reader [body]
  `(m-do [_# (ask)] [:return ~body]))

;; eof
