(in-ns 'kite-test)

(import
  [java.io
   BufferedReader
   ByteArrayInputStream
   InputStreamReader])


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

(def ^:private nil-bytes (byte-array 0))

(def ^:private ctx
  (-> {}
      (add-execution-context {})
      (add-aio-context {})))

(def port 3001)

(with-context
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
                          (when (> (count b) 0)
                            (binding [*in* (BufferedReader. (InputStreamReader. (ByteArrayInputStream. b)))]
                              (read-line)))
                          (write-socket socket
                                        response
                                        write-h
                                        write-e))]
            (write-h nil-bytes)))
        (mk-err :accept))
      (info server))
    (mk-err :server))
  (await-channel-group-termination (from-context :channel-group) 120000)
  (shutdown-channel-group (from-context :channel-group) 2000))

;; eof
