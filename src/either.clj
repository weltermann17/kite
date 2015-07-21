(in-ns 'kite)

(import
  [clojure.lang IDeref])

(require
  '[clojure.core.match.protocols :refer [IMatchLookup]])

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
    (val-at [_ k not-found]
      (case k
        ::right v
        not-found))))

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
    (val-at [_ k not-found]
      (case k
        ::left v
        not-found))))

(defn either [f g e]
  (match [e]
         [{::left v}] (f v)
         [{::right v}] (g v)))

(defn mirror [e]
  (match [e]
         [{::left v}] (right v)
         [{::right v}] (left v)))

(comment either mirror)

;; eof
