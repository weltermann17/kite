(in-ns 'kite.context)

(import
  (clojure.lang IPersistentMap))

(require
  '[clojure.reflect :refer [typename]])

(defn- reduce-readers [n readers initial]
  "Functional rules!"
  (loop [i 1
         c initial]
    (if (> i n)
      c
      (recur (inc i)
             (into {} (for [[k v] readers]
                        [k (try-or-else ((run-reader v) c) v)]))))))

(defn mk-config
  ([default config] (mk-config 5 default config))
  ([reduce-by default config]
   {:pre  [(and (instance? IPersistentMap default) (instance? IPersistentMap config))]
    :post [(instance? IPersistentMap %)]}
   (let [all (merge default config)
         readers (select-keys all (for [[k v] all :when (satisfies? Reader v)] k))
         reduced (reduce-readers reduce-by readers all)]
     (merge all reduced))))

(defn invalid-config!
  "Throws an informational exception if 'e' is not a Reader 'anymore'."
  ([e msg]
   (invalid-config! e msg {}))
  ([e msg data]
   (invalid-config! e msg data nil))
  ([e msg data cause]
   (when-not (satisfies? Reader e)
     (comment msg)
     (info! (<< "Invalid configuration: ~{msg} (value: ~{e})") data cause)))
  )

(defn check-type [t e]
  "Throws an informational exception if 'e' is not of type 't' and not a Reader."
  (when-not (instance? t e)
    (invalid-config! e (<< "'~{e}' is not a '~{(typename t)}', but a '~{(typename (type e))}'"))))

(defmacro check-cond [cond]
  "Throws an informational exception if 'cond' is not true."
  `(when-not ~cond
     (info! (str "Invalid configuration: assertion failed : " '~cond))))

;; eof
