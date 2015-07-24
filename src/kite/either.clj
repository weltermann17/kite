(in-ns 'kite)

(defprotocol Either)

(defprotocol Left
  (-left [_]))

(defprotocol Right
  (-right [_]))

(defn right [v]
  (reify
    Either
    Right
    (-right [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [_ o] (and (satisfies? Right o) (= v (-right o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Right " v))

    Pure
    (-pure [_ u] (right u))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k d] (case k ::right v d))))

(defn left [v]
  (reify
    Either
    Left
    (-left [_] v)

    IDeref
    (deref [_] v)

    Object
    (equals [_ o] (and (satisfies? Left o) (= v (-left o))))
    (hashCode [_] (hash v))
    (toString [_] (str "Left " v))

    Pure
    (-pure [_ u] (right u))

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k d] (case k ::left v d))))

(defn either [l r e]
  (matchm [e]
          [{::left v}] (l v)
          [{::right v}] (r v)))

(defn mirror [e]
  "Needed for arrows."
  (matchm [e]
          [{::left v}] (right v)
          [{::right v}] (left v)))

(comment either mirror)

;; eof
