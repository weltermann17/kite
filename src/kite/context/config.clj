(in-ns 'kite.context)

(require
  '[clojure.reflect :refer [typename]])

(defn- reduce-readers [depth-max readers]
  (loop [i 1
         c readers]
    (if (or (> i depth-max) (not-any? #(reader? %) (vals c)))
      c
      (recur (inc i)
             (into {} (for [[k v] readers]
                        [k (try-or-else (run-reader v c) v)]))))))

;; public fns

(defn merge-config
  ([existing-config new-config]
   (merge-config 10 existing-config new-config))
  ([reduce-by-max existing-config new-config]
   {:pre  [(map? existing-config)
           (map? new-config)]
    :post [(map? %)
           (not-any? (fn [v] (reader? v)) (vals %))]}
   (let [all (merge existing-config new-config)
         all-readers (into {} (for [[k v] all] [k (if (reader? v) v (reader v))]))]
     (reduce-readers reduce-by-max all-readers))))

;; error handling during configuration

(defn invalid-config!
  "Throws an informational exception if 'e' is not a Reader 'anymore'."
  ([e msg]
   (invalid-config! e msg {}))
  ([e msg data]
   (invalid-config! e msg data nil))
  ([e msg data cause]
   (when-not (reader? e)
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
