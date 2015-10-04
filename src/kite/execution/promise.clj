(in-ns 'kite.execution)

(import
  [clojure.lang IDeref])

;; protocols

(defprotocol Future
  (^Result await [_ milliseconds]
    "Should be used for testing only. 'Monadic use' or 'completion-handlers' should be preferred.
    Timeout in milliseconds must be > 0.")
  (on-complete
    [_ f]
    [_ succ fail]
    "Adds a callback executed on completion or calls it directly if already completed.
     Unlike 'succ' and 'fail' 'f' must expect a Result not a value or exception."))

(defprotocol Promise
  (complete [_ v]
    "Sets the value and executes all callbacks. Throws illegal-state! if called more than once.")
  (^Future ->future [_]
    "Returns its corresponding future."))

;; types

(declare mk-future)

(def ^:private ^:const not-yet-completed ::not-yet-completed)

(defn promise []
  (let [value (atom not-yet-completed)
        callbacks (transient [])
        future (mk-future value callbacks)]
    (reify
      Promise
      (complete [this v]
        (if (compare-and-set! value not-yet-completed v)
          (execute-all (persistent! callbacks) v)
          (illegal-state! (<< "A promise cannot be completed more than once, value already set = ~{@value}, value not accepted = ~{v}")))
        this)
      (->future [_] future)

      IDeref
      (deref [_] @value)

      Object
      (equals [this o] (test-eq this o Promise #(= @value @o)))
      (hashCode [_] (hash @value))
      (toString [_] (<< "(Promise ~{@value})")))))

;; eof
