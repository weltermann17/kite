(in-ns 'kite.control)

(defn fatal? [^Throwable e]
  (some #(instance? % e) [InterruptedException
                          LinkageError
                          ThreadDeath
                          VirtualMachineError]))

(defn fatal?! [^Throwable e]
  "When e is fatal then rethrow it."
  (when (fatal? e) (throw e)))

(defn illegal-state! [^String s]
  (throw (IllegalStateException. s)))

(defn no-such-method! [^String s]
  (throw (NoSuchMethodException. s)))

(comment no-such-method!)

;; eof
