(in-ns 'kite.http)

(import [kite.string ByteString])

;; constants

(def ^:constant ^bytes end-of-line (.getBytes "\r\n"))

(def ^:constant ^bytes end-of-headers (.getBytes "\r\n\r\n"))

(def ^:constant ^bytes space (.getBytes " "))

(def ^:constant ^bytes tab (.getBytes "\t"))

(def ^:constant ^bytes colon-delimiter (.getBytes ": "))

(def ^:constant ^bytes method-get (.getBytes "GET "))

(def ^:constant ^bytes method-head (.getBytes "HEAD "))

(def ^:constant ^bytes method-put (.getBytes "PUT "))

(def ^:constant ^bytes method-post (.getBytes "POST "))

(def ^:constant ^bytes method-delete (.getBytes "DELETE "))

(def ^:constant ^bytes method-options (.getBytes "OPTIONS "))

(def ^:constant ^bytes method-trace (.getBytes "TRACE "))

;; request parser

(defn- parse-header-line [^ByteString header-line]
  "Splits the line at ': ' and converts the name to a lower-case keyword.
  Returns a vector of header name and value."
  (let [[k v] (take-until header-line colon-delimiter)]
    [(keyword (lower-case (convert-to-string k))) v]))

(defn- parse-method [^ByteString request-line]
  "Returns a vector of the http method as a lower-case keyword and the remainder of the request-line."
  (cond
    (starts-with request-line method-get) :get
    (starts-with request-line method-post) :post
    (starts-with request-line method-put) :put
    (starts-with request-line method-head) :head
    (starts-with request-line method-delete) :delete
    (starts-with request-line method-options) :options
    (starts-with request-line method-trace) :trace
    :else (throw client-405)))

(defn next-request [^ByteString in]
  "Elegant version. Needs 30 µs for 'big'. Down to <3 µs."
  (let [[request more] (take-until in end-of-headers)
        [requestline headers] (take-until request end-of-line)]
    [(assoc
       (zipmap [:method :path :protocol] (split-delimiter requestline space))
       :headers
       (delay (into {} (map parse-header-line (split-delimiter headers end-of-line)))))
     more]))

(defn get-header ^ByteString [request header]
  "As :headers is a 'delay' access to a header within requires a deref."
  (header @(:headers request)))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(def ^String small "GET /ping HTTP/1.0\nHost: 127.0.0.1\r\n\r\n")
(def ^String big "GET /ping HTTP/1.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: en-US,en;q=0.5\r\nCache-Control: max-age=0\r\nConnection: keep-alive\r\nHost: localhost:3001\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0\r\n\r\nYYYYYYYYYY")
(def rq (byte-string (.getBytes big)))

;(println (next-request rq))
;(println @(:headers (first (next-request rq))))
(with-progress-reporting (bench (next-request rq)))

;; eof
