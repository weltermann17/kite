(in-ns 'kite-test)

(import java.nio.ByteBuffer)

(def ^:private ^:constant response
  (let [c 48
        s "HTTP/1.1 200 OK\nConnection: keep-alive\nContent-Type: text/plain\nContent-Length: 4\nDate: Wed, 11 Mar 2015 13:13:24 GMT\n\npong"
        r (reduce str (repeat c s))]
    (.getBytes ^String r)))

(with-context (-> {}
                  (add-execution-context {})
                  (add-aio-context {}))
              (let [timeout (from-context :socket-read-write-timeout)]
                (letfn [(err [prefix ^Throwable e]
                             (when-not (= socket-eof-exception e) (error prefix (type e) (.getMessage e) e)))
                        (mk-err [prefix] (partial err prefix))]
                  (socket-server
                    3001
                    (fn [server]
                      (let [read-e (mk-err :read)
                            write-e (mk-err :write)]
                        (accept server
                                (fn [socket]
                                  (configure-socket socket)
                                  (letfn [(write-h [^ByteBuffer b]
                                                   (read-socket socket
                                                                timeout
                                                                b
                                                                read-h
                                                                read-e))
                                          (read-h [^ByteBuffer b]
                                                  (doto b (.clear) (.put response) (.flip))
                                                  (write-socket socket
                                                                b
                                                                timeout
                                                                write-h
                                                                write-e))]
                                    (write-h (ByteBuffer/allocateDirect 10000))))
                                (mk-err :accept))
                        (info server)))
                    (mk-err :server)))
                (await-channel-group-termination (from-context :channel-group) 120000)
                (shutdown-channel-group (from-context :channel-group))))

;; eof
