(in-ns 'kite)

(defprotocol Either)

(defprotocol Left)

(defprotocol Right)

(defn right [v]
  (reify
    Either
    Right

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (equal? this o Right #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Right " v))

    Pure
    (-pure [_ u] (right u))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k d] (if (= Right k) v d))))

(defn left [v]
  (reify
    Either
    Left

    IDeref
    (deref [_] v)

    Object
    (equals [this o] (equal? this o Left #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Left " v))

    Pure
    (-pure [_ u] (right u))

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k d] (if (= Left k) v d))))

(defn either [l r e]
  (matchm [e]
          [{Left v}] (l v)
          [{Right v}] (r v)))

(defn mirror [e]
  "Needed for arrows."
  (matchm [e]
          [{Left v}] (right v)
          [{Right v}] (left v)))

(comment either mirror)

;; eof
