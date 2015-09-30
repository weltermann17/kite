(in-ns 'kite.aio)

(defn- default-client-initial-pool-size []
  (reader (* 0 3)))

(defn- default-client-maximum []
  (reader (* 8 1)))

(defn- default-client-per-remoteaddress-maximum []
  (reader (* 1 2)))

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

(defn- default-client-per-remoteaddress-pool []
  (reader (fn [] (atom {}))))

(defn comm []
  "
  limits:
  - max-clients-per-remote-address
  - max-pooled-closed-clients
  - max-clients ?

  pools:
  - pool-of-closed-but-configured-clients
  - hashmap of remoteaddress -> pool-of-open-clients-to-this-remote-address
  - all pools must be absolutely threadsafe
  - closed-pool could be in hashmap with address 0.0.0.0:0

  operations:
  - get open client to address, if get returns nil get closed client from default pool
  - if not nil get head of per-address-pool, if nil default pool
  - busy wait when max-clients is reached, maybe with a yield 0

  fns:
  - release-client-to-default-pool (all closed clients)
  - acquire-client-from-default-pool
  - release-client-to-per-remoteaddress-pool, if max reached to default
  - acquire-client-from-per-remoteaddress-pool, if nil from default

  backpressure:
  - when max-client-per-remoteaddress is reached do busy wait

  workflow:
  - issue request in an endless loop with succ on response else fail
  - this will quickly reach max-client-per-remoteaddress


  ")

;; helpers

(defn connected? [^AsynchronousSocketChannel client]
  (not-nil? (.getRemoteAddress client)))

(defn release-client-to-default-pool [^AsynchronousSocketChannel client]
  (let [pool (from-context :client-pool)
        mx (from-context :client-maximum-pool-size)]
    (when (< (count @pool) mx)
      (swap! pool conj client))))

(defn release-client-to-per-remoteaddress-pool
  "Add to pool key/value: remoteaddress/client."
  [^AsynchronousSocketChannel client]
  (let [pool (from-context :client-per-remoteaddress-pool)
        mx (from-context :client-per-remoteaddress-maximum)]
    (if-let [remoteaddress (cast InetSocketAddress (.getRemoteAddress client))]
      (if-let [remote (get @pool remoteaddress)]
        (when (< (count @remote) mx)
          (swap! pool assoc remoteaddress (swap! remote conj client)))
        (swap! pool assoc remoteaddress (atom [client])))
      (error "not handled")))                               ;(release-client-to-default-pool client)))
  (info "after release" (from-context :client-per-remoteaddress-pool)))

(defn acquire-client-from-default-pool ^AsynchronousSocketChannel []
  "Will busy wait if :client-maximum is reached."
  (let [pool (from-context :client-pool)
        mx (from-context :client-maximum)]
    (loop []
      (let [[^AsynchronousSocketChannel head & rest :as all] @pool]
        (if head
          (if (compare-and-set! pool all rest)
            (do (info "rest" rest) head)
            (recur))
          (if (< 1 mx)
            (configure-socket (AsynchronousSocketChannel/open (from-context :channel-group)))
            (recur)))))))

(defn acquire-client-from-per-remoteaddress-pool ^AsynchronousSocketChannel [^InetSocketAddress remoteaddress]
  (info "try get" remoteaddress (hash remoteaddress))
  (let [pool (from-context :client-per-remoteaddress-pool)]
    (if-let [remote (get @pool remoteaddress)]
      (do (info "remoteaddr" remote)
          (loop []
            (let [[^AsynchronousSocketChannel head & rest :as all] @remote]
              (info "head" head rest)
              (if head
                (if (compare-and-set! remote all rest)
                  head
                  (recur))
                (acquire-client-from-default-pool)))))
      (do (error "get failed") (acquire-client-from-default-pool)))))

;; eof
