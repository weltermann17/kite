(in-ns 'kite-test)

(import
  (java.io EOFException)
  (java.nio ByteBuffer))

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(def ctx (-> {}
             (add-execution-context {})
             (add-aio-context {})))

;;(with-context ctx (socket-server 3001 nil nil))

(with-context ctx
  (let [timeout (from-context :socket-read-write-timeout)]
    (letfn [(error-handler
              [^String prefix ^Throwable e]
              (when-not (instance? EOFException e) (error prefix e)))]
      (socket-server
        3001
        (fn [server]
          (info server)
          (accept server
                  (fn [socket]
                    (configure-socket socket)
                    (letfn [(read-repeat
                              [buffer]
                              (read-socket socket
                                           buffer
                                           timeout
                                           (fn [^ByteBuffer b]
                                             (.clear b)
                                             (.put b default-response)
                                             (.flip b)
                                             (write-socket socket
                                                           b
                                                           timeout
                                                           (fn [^ByteBuffer b]
                                                             (.clear b)
                                                             (read-repeat b))
                                                           (partial error-handler "write")))
                                           (partial error-handler "read")))]
                      (read-repeat (ByteBuffer/allocateDirect 10000))))
                  (partial error-handler "socket")))
        (partial error-handler "server"))))
  (await-channel-group-termination (from-context :channel-group) 120000))

;; eof
