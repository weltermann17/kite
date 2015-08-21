(in-ns 'kite.context)

(import
  [clojure.lang IDeref])

(defn- thread-inheritable [value]
  (doto (proxy [InheritableThreadLocal IDeref] [] (deref [] (.get this))) (.set value)))

(def ^:dynamic *implicit-context* (thread-inheritable []))

(defn from-context [f] (f @*implicit-context*))

(defmacro inheritable-binding
  [bindings & body]
  `(let [s# (fn [bindings-map#] (doseq [[v# value#] bindings-map#] (.set v# value#)))
         inner-bindings# (hash-map ~@bindings)
         outer-bindings# (into {} (for [[k# _#] inner-bindings#] [k# (deref k#)]))]
     (try
       (s# inner-bindings#)
       ~@body
       (finally
         (s# outer-bindings#)))))

(defmacro with-context
  [m & body]
  `(inheritable-binding [*implicit-context* ~m] ~@body))

;; eof
