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
    [(keyword (lower-case k)) v]))

(defn parse-request [^ByteString b]
  (let [[req remainder] (take-until b end-of-headers)
        [head & tail] (split-with-delimiter req end-of-line)]
    [(merge
       (zipmap [:method :path :protocol] (split-with-delimiter head space))
       (into {} (map parse-header tail)))
     ;; handle request body if any
     remainder]))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(defn small ^String [] "GET /ping HTTP/1.0\nHost: 127.0.0.1\r\n\r\n")
(def big "GET /ping HTTP/1.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: en-US,en;q=0.5\r\nCache-Control: max-age=0\r\nConnection: keep-alive\r\nHost: localhost:3001\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0\r\n\r\n")
(def rq (byte-string (.getBytes (small))))

;(with-progress-reporting (bench (parse-request rq)))

;; eof
