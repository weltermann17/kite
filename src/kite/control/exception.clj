(in-ns 'kite.control)

(import
  (clojure.lang ExceptionInfo))

;; fatal handling

(defn fatal? [e]
  "A fatal exception should lead to the termination of the jvm."
  (any? #(instance? % e) [InterruptedException
                          LinkageError
                          ThreadDeath
                          VirtualMachineError]))

(defn fatal?!
  ([e]
   "If e is fatal then it is rethrown else it is simply returned."
   (fatal?! e e))
  ([e v]
   "If e is fatal then it is rethrown else v is returned."
   (if (fatal? e) (throw e) v)))

;; most common exceptions, for my kind of code that is

(defn illegal-state! [^String s & fs]
  (throw (IllegalStateException. (str s fs))))

(defn illegal-argument! [^String s & fs]
  (throw (IllegalArgumentException. (str s fs))))

(defn index-out-of-bounds! [^String s & fs]
  (throw (IndexOutOfBoundsException. (str s fs))))

(defn no-such-method! [^String s & fs]
  "Mainly used to signal 'not yet implemented' during development."
  (throw (NoSuchMethodException. (str s fs))))

;; handle exceptional information

(defn info!
  "Throws an 'informational exception'.  This is used for control flow in exceptional cases."
  ([msg]
   (info! msg {}))
  ([msg data]
   (throw (ex-info msg data)))
  ([msg data cause]
   (throw (ex-info msg data cause))))

(defn info? [^Throwable e]
  (instance? ExceptionInfo e))

(defn info?!
  ([^Throwable e]
   "If e is exceptional information then it is rethrown else it is simply returned."
   (info?! e e))
  ([^Throwable e v]
   "If e is exceptional information then it is rethrown else v is returned."
   (if (info? e) (throw e) v)))

;; helpers

(defmacro try-or-else [body value]
  "Returns the result of 'body' or in case of a non-fatal and non-informational
  exception 'value'. Exceptions that are either fatal or informational are rethrown."
  `(try ~body (catch Throwable e# ((comp fatal?! info?!) e# ~value))))

;; eof
