(in-ns 'kite.context)

;; stolen from https://github.com/sjl/caves

(defn- make-fnmap
  [fns]
  (into {} (for [[label fntail] (map (juxt first rest) fns)]
             [(keyword label)
              `(fn ~@fntail)])))

(defn- make-fnheads
  [fns]
  (map #(take 2 %) fns))


(defmacro defaspect
  [label & fns]
  (let [fnmap (make-fnmap fns)
        fnheads (make-fnheads fns)]
    `(do
       (defprotocol ~label
         ~@fnheads)
       (def ~label
         (with-meta ~label {:defaults ~fnmap})))))

(defmacro add-aspect
  [entity aspect & fns]
  (let [fnmap (make-fnmap fns)]
    `(extend ~entity ~aspect (merge (:defaults (meta ~aspect))
                                    ~fnmap))))

;; eof
