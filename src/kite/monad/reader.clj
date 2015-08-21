(in-ns 'kite.monad)

(defprotocol Reader
  (run-reader [_ e]))

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

(def ^:private ask-
  (mk-reader (fn [e] (identity e))))

(defn ask [] ask-)

(defn asks [f]
  (mk-reader (fn [e] (f e))))

(defn local [f g]
  (mk-reader (fn [e] (run-reader g (f e)))))

;; macro

(defmacro reader [body]
  `(m-do [_# (ask)] [:return ~body]))

;; eof
