(in-ns 'kite-test)

(expect 0 (byte-array-index-of (.getBytes "aaaaa") (.getBytes "a")))
(expect 0 (byte-array-index-of (.getBytes "aaaaa") (.getBytes "aa")))
(expect 1 (byte-array-index-of (.getBytes "baaaa") (.getBytes "a")))
(expect 2 (byte-array-index-of (.getBytes "bbaaa") (.getBytes "aa")))
(expect -1 (byte-array-index-of (.getBytes "bbbaa") (.getBytes "aaa")))
(expect 7 (byte-array-index-of (.getBytes "aaaaabbaaa") (.getBytes "aaa") 4))
(expect -1 (byte-array-index-of (.getBytes "aaaaabbaaa") (.getBytes "aaa") 4 9))
(expect 7 (byte-array-index-of (.getBytes "aaaaabbaaa") (.getBytes "aaa") 4 10))

(def ^:private ^:constant response
  (let [c 48
        s "HTTP/1.1 200 OK\nConnection: keep-alive\nContent-Type: text/plain\nContent-Length: 4\nDate: Wed, 11 Mar 2015 13:13:24 GMT\n\npong"
        r (reduce str (repeat c s))]
    (.getBytes ^String r)))

(defn- err [prefix ^Throwable e]
  (when-not (harmless-socket-exception? e) (error prefix (type e) (.getMessage e) e)))

(defn- mk-err [prefix] (partial err prefix))

(def ^:private read-e (mk-err :read))

(def ^:private write-e (mk-err :write))

(def ^:private ^bytes nil-bytes (byte-array 0))

(def ^:private ^bytes end-of-header (.getBytes "\r\n\r\n"))

(info "eoh" (vec end-of-header))

(def ^:private ctx
  (-> {}
      (add-execution-context {})
      (add-aio-context {})))

(def port 3001)

(expect nil (with-context
              ctx
              (socket-server
                port
                (fn [server]
                  (accept
                    server
                    (fn [socket]
                      (letfn [(write-h [_]
                                       (read-socket socket
                                                    read-h
                                                    read-e))
                              (read-h [^bytes b]
                                      ;(byte-array-index-of b end-of-header)
                                      (info (.indexOf (String. b) "\r\n\r\n"))
                                      (write-socket socket
                                                    response
                                                    write-h
                                                    write-e))]
                        (write-h nil-bytes)))
                    (mk-err :accept))
                  (info server))
                (mk-err :server))
              (await-channel-group-termination (from-context :channel-group) 119000)
              (shutdown-channel-group (from-context :channel-group) 1000))
        )

;; eof
