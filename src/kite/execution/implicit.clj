(in-ns 'kite.execution)

(import
  [clojure.lang IDeref])

;; A combination of these 3 articles:
;; https://aphyr.com/posts/240-configuration-and-scope
;; http://stevenskelton.ca/threadlocal-variables-scala-futures/
;; http://code.hootsuite.com/logging-contextual-info-in-an-asynchronous-scala-application/

(defn- inheritable-thread-local* [v]
  (proxy [InheritableThreadLocal IDeref] []
    (initialValue [] (v))
    (deref [] (.get ^InheritableThreadLocal this))))

(defmacro ^:private inheritable-thread-local [& body]
  `(inheritable-thread-local* (fn [] ~@body)))

(defmacro inheritable-binding
  [bindings & body]
  `(let [bind# (fn [bindings#]
                 (doseq [[v# value#] bindings#] (.set ^InheritableThreadLocal v# value#)))
         inner-bindings# (hash-map ~@bindings)
         outer-bindings# (into {} (for [[k# _#] inner-bindings#] [k# (deref k#)]))]
     (try
       (bind# inner-bindings#)
       ~@body
       (finally
         (bind# outer-bindings#)))))

;; the implicit context and its accessors

(def ^:private implicit-execution-contexts
  "Not to be used directly. Holds a map of ExecutorService -> Context."
  (atom {}))

(def implicit-context
  ;; todo: should be private
  "Not to be used directly, should be accessed with 'with-context', 'from-context' and 'all-context'."
  (inheritable-thread-local {}))

(defn- add-implicit-context [executor context]
  (swap! implicit-execution-contexts assoc executor context))

(defn- remove-implicit-context [executor]
  (swap! implicit-execution-contexts dissoc executor))

(defn- reset-implicit-context [e]
  (.set ^InheritableThreadLocal implicit-context (@implicit-execution-contexts e)))

;; public fns

(defn all-context []
  @implicit-context)

(defn from-context [f]
  (f @implicit-context))

(defn with-context* [m]
  (let [k (m :executor)]
    (when k (add-implicit-context k m))))

(defmacro with-context
  [m & body]
  `(do (with-context* ~m)
       (inheritable-binding [implicit-context ~m] ~@body)))

;; eof
