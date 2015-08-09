(in-ns 'kite.context)

;; utility fns

(defn config-value [k f config]
  "Will return 'Success value' if k exists and value can be converted by f else 'Failure reason'."
  (fmap f (result (let [v (k config)]
                    (if-not (nil? v)
                      v
                      (index-out-of-bounds! k))))))

(defn config-boolean [k config]
  (config-value k boolean config))

(defn config-int [k config]
  (config-value k int config))

(defn config-long [k config]
  (config-value k long config))

(defn config-double [k config]
  (config-value k double config))

(defn config-bigdec [k config]
  (config-value k bigdec config))

(defn config-num [k config]
  (config-value k num config))

(defn config-str [k config]
  (config-value k str config))

;; eof
