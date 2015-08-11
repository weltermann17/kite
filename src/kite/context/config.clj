(in-ns 'kite.context)

(import
  (clojure.lang IPersistentMap))

(require
  '[clojure.reflect :refer [typename]])

(defn- reduce-readers [n readers]
  (loop [i 1
         c readers]
    (if (or (> i n) (not-any? #(satisfies? Reader %) (vals c)))
      c
      (recur (inc i)
             (into {} (for [[k v] readers]
                        [k (try-or-else (run-reader v c) v)]))))))

;; public fns

(defn merge-config
  ([default config]
   (merge-config 10 default config))
  ([reduce-by default config]
   {:pre  [(instance? IPersistentMap default)
           (instance? IPersistentMap config)]
    :post [(instance? IPersistentMap %)
           (not-any? (fn [v] (satisfies? Reader v)) (vals %))]}
   (let [all (merge default config)
         all-readers (into {} (for [[k v] all] [k (if (satisfies? Reader v) v (reader v))]))]
     (reduce-readers reduce-by all-readers))))

;; error handling during configuration

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
  "Throws an informational exception if 'e' is not of type 't', but only if 'e' is not a Reader."
  (when-not (instance? t e)
    (invalid-config! e (<< "'~{e}' is not a '~{(typename t)}', but a '~{(typename (type e))}'"))))

(defmacro check-cond [cond]
  "Throws an informational exception if 'cond' is not true."
  `(when-not ~cond
     (info! (str "Invalid configuration: assertion failed : " '~cond))))

;; eof
