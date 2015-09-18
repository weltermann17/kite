(in-ns 'kite.concurrent)

(import
  [clojure.lang IDeref])

;; protocols

(defprotocol Future
  (^Result await [_ milliseconds]
    "Should be used for testing only. 'Monadic use' or 'completion-handlers' should be preferred.
    Timeout in milliseconds must be > 0.")
  (on-complete [_ f]
    "Adds a callback executed on completion or calls it directly if already completed.
     'f' must expect a Result.")
  (on-success [_ f]
    "Adds a callback executed only on success. 'f' must expect the value inside the Success.")
  (on-failure [_ f]
    "Adds a callback executed only on failure. 'f' must expect the value inside the Failure."))

(defprotocol Promise
  (complete [_ v]
    "Set the value and execute all callbacks. Throws illegal-state! if called more than once.")
  (^Future ->future [_]
    "Returns its corresponding future."))

;; types

(declare mk-future)

(def ^:private ^:const not-yet-completed ::not-yet-completed)

(defn promise []
  (let [value (atom not-yet-completed)
        callbacks (atom [])
        future (mk-future value callbacks)]
    (reify
      Promise
      (complete [this v]
        (if (compare-and-set! value not-yet-completed v)
          (execute-all @callbacks v)
          (illegal-state! (<< "A promise cannot be completed more than once, value already set = ~{@value}, value not accepted = ~{v}")))
        this)
      (->future [_] future)

      IDeref
      (deref [_] @value)

      Object
      (equals [this o] (test-eq this o Promise #(= @value @o)))
      (hashCode [_] (hash @value))
      (toString [_] (<< "Promise ~{@value}")))))

;; eof
