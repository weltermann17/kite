(in-ns 'kite.concurrent)

(import
  [clojure.lang IDeref])

;; protocols

(defprotocol Future
  (^Result await [_ milliseconds]
    "Should be used for testing only. 'Monadic use' should be preferred. Timeout in milliseconds must be > 0.")
  (on-complete [_ f]
    "Adds callbacks executed on completion or calls them directly if already completed."))

(defprotocol Promise
  (complete [_ v]
    "Throws illegal-state! if called more than once.")
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
        (when-not (compare-and-set! value not-yet-completed v)
          (illegal-state! (<< "A promise cannot be completed more than once, value already set = ~{@value}, value not accepted = ~{v}")))
        this)
      (->future [_] future)

      IDeref
      (deref [_] "Actually a double deref, because value is a volatile." @value)

      Object
      (equals [this o] (test-eq this o Promise #(= @value @o)))
      (hashCode [_] (hash @value))
      (toString [_] (<< "Promise ~{@value}"))

      Reader
      (run-reader [_ env]
        (run-reader (execute-all (persistent! callbacks) @value) env)))))

;; eof