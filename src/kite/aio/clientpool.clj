(in-ns 'kite.aio)

(defn- default-client-per-host-maximum-size []
  (reader (* 1 1024)))

(defn- default-client-initial-pool-size []
  (reader (* 1 1024)))

(defn- default-client-pool []
  (m-do [poolsize (asks :client-initial-pool-size)
         keepalive (asks :socket-keep-alive)
         nodelay (asks :socket-no-delay)
         rcvbs (asks :socket-receive-buffer-size)
         sndbs (asks :socket-send-buffer-size)]
        [:let
         _ (check-type Long poolsize)
         _ (check-cond (>= poolsize 0))]
        [:return
         (fn [] (let [channel-group (from-context :channel-group)]
                  (atom (doall
                          (for [_ (range poolsize)]
                            (doto (AsynchronousSocketChannel/open channel-group)
                              (.setOption StandardSocketOptions/SO_KEEPALIVE keepalive)
                              (.setOption StandardSocketOptions/SO_RCVBUF (Integer/valueOf (int rcvbs)))
                              (.setOption StandardSocketOptions/SO_SNDBUF (Integer/valueOf (int sndbs)))
                              (.setOption StandardSocketOptions/TCP_NODELAY nodelay)))))))]))

(defn release-client [^AsynchronousSocketChannel client]
  "Closes the client and releases it back to the pool."
  (let [pool (from-context :client-pool)]
    (swap! pool conj (close-socket client))))

(defn acquire-client ^AsynchronousSocketChannel []
  "Returns an unconnected client from the pool or if the pool is empty returns a newly created and configured one."
  (let [pool (from-context :client-pool)]
    (loop []
      (let [[^AsynchronousSocketChannel head & rest :as all] @pool]
        (if head
          (if (compare-and-set! pool all rest)
            (do (info "from pool" (count @pool)) head)
            (recur))
          (configure-socket (AsynchronousSocketChannel/open (from-context :channel-group))))))))

;; eof
