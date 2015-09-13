(in-ns 'kite-test)

(def ^:constant response
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
                      (let [read-err (mk-err :read)
                            write-err (mk-err :write)]
                        (info server)
                        (accept server
                                (fn [socket]
                                  (configure-socket socket)
                                  (letfn [(write-h [_]
                                                   (read-socket socket
                                                                timeout
                                                                read-h
                                                                read-err))
                                          (read-h [^bytes _]
                                                  (write-socket socket
                                                                response
                                                                timeout
                                                                write-h
                                                                write-err))]
                                    (write-h nil)))
                                (mk-err :accept))))
                    (mk-err :server)))
                (await-channel-group-termination (from-context :channel-group) 120000)
                (shutdown-channel-group (from-context :channel-group))))

;; eof
