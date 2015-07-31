(in-ns 'kite.context)

(import
  (clojure.lang IPersistentMap))

(defn- reduce-readers [n readers initial]
  (loop [i 1
         c initial]
    (if (> i n)
      c
      (recur
        (inc i)
        (into {} (for
                   [[k v] readers]
                   [k (try ((run-reader v) c) (catch Throwable _ v))]))))))

(defn mk-config
  ([default config] (mk-config 5 default config))
  ([reduce-by default config]
   {:pre  [(and (instance? IPersistentMap default) (instance? IPersistentMap config))]
    :post [(instance? IPersistentMap %)]}
   (let [all (merge default config)
         readers (select-keys all (for [[k v] all :when (satisfies? Reader v)] k))
         reduced (reduce-readers reduce-by readers all)]
     (merge all reduced))))

;; eof
