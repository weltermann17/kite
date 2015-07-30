(in-ns 'kite.control)

(defn fatal? [^Throwable e]
  "A fatal exception should lead to the termination of the jvm."
  (some #(instance? % e) [InterruptedException
                          LinkageError
                          ThreadDeath
                          VirtualMachineError]))

(defn fatal?! [^Throwable e]
  "If e is fatal then it is rethrown else it is simply returned."
  (if (fatal? e) (throw e) e))

(defn illegal-state! [^String s]
  (throw (IllegalStateException. s)))

(defn no-such-method! [^String s]
  "Mainly used to signal 'not yet implemented' during development."
  (throw (NoSuchMethodException. s)))

;; eof
