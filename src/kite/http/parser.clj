(in-ns 'kite.http)

;; constants

(def ^:constant ^bytes end-of-line (.getBytes "\r\n"))

(def ^:constant ^bytes end-of-headers (.getBytes "\r\n\r\n"))

(def ^:constant ^bytes space (.getBytes " "))

(def ^:constant ^bytes tab (.getBytes "\t"))

(def ^:constant ^bytes colon-delimiter (.getBytes ": "))

(def ^:constant ^bytes method-get (.getBytes "GET"))

(def ^:constant ^bytes method-head (.getBytes "HEAD"))

(def ^:constant ^bytes method-put (.getBytes "PUT"))

(def ^:constant ^bytes method-post (.getBytes "POST"))

(def ^:constant ^bytes method-delete (.getBytes "DELETE"))

(def ^:constant ^bytes method-options (.getBytes "OPTIONS"))

(def ^:constant ^bytes method-trace (.getBytes "TRACE"))

;; request parser

(defn- parse-header [^ByteString in]
  (let [[k v] (take-until in colon-delimiter)]
    [k v]))

(defn parse-request [^ByteString b]
  (let [[req remainder] (take-until b end-of-headers)
        lines (split-with-delimiter req end-of-line)]
    (merge
      (zipmap [:method :path :protocol] (split-with-delimiter (first lines) space))
      (into {} (map parse-header (rest lines))))))

;; eof
