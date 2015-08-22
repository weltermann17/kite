(in-ns 'kite.context)

(import
  [clojure.lang IDeref])

;; A combination of these 2 articles
;; https://aphyr.com/posts/240-configuration-and-scope
;; http://stevenskelton.ca/threadlocal-variables-scala-futures/

(defmacro inheritable-binding
  [bindings & body]
  `(let [bind# (fn [bindings#] (doseq [[v# value#] bindings#] (.set v# value#)))
         inner-bindings# (hash-map ~@bindings)
         outer-bindings# (into {} (for [[k# _#] inner-bindings#] [k# (deref k#)]))]
     (try
       (bind# inner-bindings#)
       ~@body
       (finally
         (bind# outer-bindings#)))))

(defn inheritable-thread-local [value]
  (doto (proxy [InheritableThreadLocal IDeref] [] (deref [] (.get this))) (.set value)))

;; the implicit context and its accessors

(def implicit-context
  "Not to be used directly, should be accessed with 'with-context', 'from-context' and 'all-context'."
  (inheritable-thread-local {}))

(defmacro with-context
  [m & body]
  `(inheritable-binding [implicit-context ~m] ~@body))

(defn all-context [] @implicit-context)

(defn from-context [f] (f @implicit-context))

;; eof
