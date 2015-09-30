(in-ns 'kite.aio)

(defn default-aio-configuration []
  {
   :channel-group                    (default-channel-group)
   :socket-reuse-address             (default-socket-reuse-address)
   :socket-backlog                   (default-socket-backlog)
   :socket-receive-buffer-size       (default-socket-receive-buffer-size)
   :socket-send-buffer-size          (default-socket-send-buffer-size)
   :socket-keep-alive                (default-socket-keep-alive)
   :socket-no-delay                  (default-socket-no-delay)
   :socket-read-write-timeout        (default-socket-read-write-timeout)
   :byte-buffer-pool                 (default-byte-buffer-pool)
   :byte-buffer-size                 (default-byte-buffer-size)
   :byte-buffer-initial-pool-size    (default-byte-buffer-initial-pool-size)
   :client-per-remoteaddress-maximum (default-client-per-remoteaddress-maximum)
   :client-initial-pool-size         (default-client-initial-pool-size)
   :client-maximum                   (default-client-maximum)
   :client-pool                      (default-client-pool)
   :client-per-remoteaddress-pool    (default-client-per-remoteaddress-pool)
   })

;; context

(defn add-aio-context [context config]
  (let [c (merge-config (default-aio-configuration) config)]
    (merge context
           {:config                           c
            :channel-group                    ((:channel-group c) (context :executor))
            :socket-read-write-timeout        (:socket-read-write-timeout c)
            :byte-buffer-size                 (:byte-buffer-size c)
            :byte-buffer-pool                 ((:byte-buffer-pool c))
            :client-per-remoteaddress-maximum (:client-per-remoteaddress-maximum c)
            :client-maximum                   (:client-maximum c)
            :client-pool                      ((:client-pool c))
            :client-per-remoteaddress-pool    ((:client-per-remoteaddress-pool c))})))

;; eof
