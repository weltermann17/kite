(in-ns 'kite.monad)

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
    (equals [this o] (test-eq this o Right #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (<< "Right ~{v}"))

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
    (equals [this o] (test-eq this o Left #(= v @o)))
    (hashCode [_] (hash v))
    (toString [_] (str "Left ~{v}"))

    Pure
    (-pure [_ u] (right u))

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k d] (if (= Left k) v d))))

(defn right? [e rt lt]
  (matchm [e]
          [{Right v}] (rt v)
          [{Left v}] (lt v)))

(defn mirror [e]
  "Needed for arrows. Arrows?"
  (matchm [e]
          [{Left v}] (right v)
          [{Right v}] (left v)))

(comment right? mirror)

;; eof
