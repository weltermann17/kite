(in-ns 'kite.aio)

(defn default-aio-configuration []
  {
   :channel-group              (default-channel-group)
   :socket-reuse-address       (default-socket-reuse-address)
   :socket-backlog             (default-socket-backlog)
   :socket-receive-buffer-size (default-socket-receive-buffer-size)
   :socket-send-buffer-size    (default-socket-send-buffer-size)
   :socket-keep-alive          (default-socket-keep-alive)
   :socket-no-delay            (default-socket-no-delay)
   :socket-read-write-timeout  (default-socket-read-write-timeout)
   })

;; context

(defn add-aio-context [context initial-config]
  (with-context context
    (let [c (merge-config (default-aio-configuration) initial-config)
          ^AsynchronousChannelGroup g ((:channel-group c))
          ^Long t (:socket-read-write-timeout c)]
      (merge context
             {:config                    c
              :channel-group             g
              :socket-read-write-timeout t}))))

;; eof
