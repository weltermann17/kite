(in-ns 'kite.aio)

(defn- default-client-maximum []
  (reader (* 1 512)))

(defn- default-client-pool []
  (reader (fn [] (atom {}))))

(defn- default-client-counter []
  (reader (fn [] (atom 0))))

(defn- mk-client []
  (let [mx (from-context :client-maximum)
        counter (from-context :client-counter)]
    (when (< @counter mx)
      (swap! counter inc) (info "inc" @counter)
      (configure-socket (AsynchronousSocketChannel/open (from-context :channel-group))))))

(defn connected? [^AsynchronousSocketChannel client]
  (and client (not (nil? (.getRemoteAddress client))) (.isOpen client)))

(defn release-client [^AsynchronousSocketChannel client]
  (if-let [remoteaddress (.getRemoteAddress client)]
    (let [pool (from-context :client-pool)]
      (if-let [remote (get @pool remoteaddress)]
        (swap! remote conj client)
        (swap! pool assoc remoteaddress (atom (cons client nil)))))
    (do
      (close-socket client)
      (swap! (from-context :client-counter) dec))))

(defn release-client-safely [^AsynchronousSocketChannel client]
  "Adds an expensive check whether the client has already been released.
  Use this during development, but not in production."
  (if-let [remoteaddress (.getRemoteAddress client)]
    (let [pool (from-context :client-pool)]
      (if-let [remote (get @pool remoteaddress)]
        (do
          (if (any? #(= % client) @remote)
            (warn "release-client-safely: client already in pool" client)
            (swap! remote conj client)))
        (swap! pool assoc remoteaddress (atom (cons client nil)))))
    (do
      (close-socket client)
      (swap! (from-context :client-counter) dec))))

(defn acquire-client ^AsynchronousSocketChannel [^InetSocketAddress remoteaddress]
  (let [pool (from-context :client-pool)]
    (if-let [remote (get @pool remoteaddress)]
      (loop []
        (let [[^AsynchronousSocketChannel head & rest :as all] @remote]
          (if (connected? head)
            (if (compare-and-set! remote all rest)
              head
              (recur))
            (do
              (when head
                (close-socket head)
                (swap! (from-context :client-counter) dec))
              (if-let [client (mk-client)]
                client
                (recur))))))
      (mk-client))))

;; eof
