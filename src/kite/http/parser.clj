(in-ns 'kite.http)

;; request

(defn- parse-header-line [^ByteString header-line]
  "Splits the line at ': ' and converts the name to a lower-case keyword.
  Returns a vector of header name and value."
  (let [[k v] (take-until header-line colon-delimiter)]
    [(keyword (lower-case (convert-to-string k))) v]))

(defn- parse-method [^ByteString method]
  "Returns the http method as a lower-case keyword."
  (cond
    (starts-with method method-get) :get
    (starts-with method method-post) :post
    (starts-with method method-put) :put
    (starts-with method method-head) :head
    (starts-with method method-delete) :delete
    (starts-with method method-options) :options
    (starts-with method method-trace) :trace
    :else (throw client-405)))

(defn- parse-protocol [^ByteString protocol]
  "Returns the http protocol as a lower-case keyword."
  (cond
    (starts-with protocol http-1-1) :http-1-1
    (starts-with protocol http-1-0) :http-1-0
    :else (throw server-505)))

(defn next-request [^ByteString b]
  "Returns a vector of requests. Handles pipe-lined requests with special handling for benchmark situations."
  (loop [result []
         previous nil
         previouslines nil
         in b]
    (if-let [rest 42]                                       ; (starts-with in previouslines)]
      (let [x (count in) y (count previouslines)]
        (if (= 0 (mod x y))
          (conj result (repeat (/ x y) previous))
          (recur (conj result previous) previous previouslines rest)))
      (if (blank? in)
        result
        (let [[requestlines more] (take-until in end-of-headers)
              [firstline headers] (take-until requestlines end-of-line)
              [method path protocol] (split-delimiter firstline space)
              request {:method   (parse-method method)
                       :protocol (parse-protocol protocol)
                       :path     (split-delimiter path slash)
                       :headers  (delay (into {} (map parse-header-line (split-delimiter headers end-of-line))))
                       }
              ]
          (if (blank? more)
            (conj result request)
            (recur (conj result request) request requestlines more)))))))

(defn get-header ^ByteString [request header]
  "As :headers is a 'delay' access to a header within requires a deref."
  (header @(:headers request)))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(def ^String small "GET /ping HTTP/1.1\nHost: 127.0.0.1\r\n\r\n")
(def ^String big "GET /ping HTTP/1.1\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: en-US,en;q=0.5\r\nCache-Control: max-age=0\r\nConnection: keep-alive\r\nHost: localhost:3001\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0\r\n\r\n")
(def ^String verybig "GET /ping HTTP/1.1\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: en-US,en;q=0.5\r\nCache-Control: max-age=0\r\nConnection: keep-alive\r\nHost: localhost:3001\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0\r\n\r\nGET /ping HTTP/1.1\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: en-US,en;q=0.5\r\nCache-Control: max-age=0\r\nConnection: keep-alive\r\nHost: localhost:3001\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0\r\n\r\nGET /ping HTTP/1.1\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: en-US,en;q=0.5\r\nCache-Control: max-age=0\r\nConnection: keep-alive\r\nHost: localhost:3001\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0\r\n\r\n")
(def rq (byte-string (.getBytes verybig)))

(println "requests" (next-request rq))
;(println @(:headers (first (next-request rq))))
;(with-progress-reporting (bench (next-request rq)))

;; eof
