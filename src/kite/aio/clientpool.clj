(in-ns 'kite.aio)

(defn- default-client-maximum []
  (reader (* 1 1024)))

(defn- default-client-pool []
  (reader (fn [] (atom {}))))

(defn- default-client-counter []
  (reader (fn [] (atom 0))))

(def busycounter (atom 0))

(defn- mk-client []
  "Creates backpressure if :client-maximum is reached."
  (let [mx (from-context :client-maximum)
        counter (from-context :client-counter)]
    (loop []
      (if (< @counter mx)
        (do
          (swap! counter inc) (info "inc" @counter)
          (configure-socket (AsynchronousSocketChannel/open (from-context :channel-group))))
        (do
          (swap! busycounter inc) (let [c @busycounter] (when (= 0 (mod c 1000000)) (info "busy" c)))
          ;(Thread/yield)
          (recur))))))

(def relcounter (atom 0))
(def acqcounter (atom 0))

(defn connected? [^AsynchronousSocketChannel client]
  (not (nil? (.getRemoteAddress client))))

(defn release-client [^AsynchronousSocketChannel client]
  (let [pool (from-context :client-pool)
        counter (from-context :client-counter)]
    (if-let [remoteaddress (.getRemoteAddress client)]
      (do
        (swap! relcounter inc)
        (if-let [remote (get @pool remoteaddress)]
          (do (swap! remote conj client) (when (= 0 (mod @relcounter 10000000)) (info "remotelist" (count @remote) "conn" @counter)))
          (swap! pool assoc remoteaddress (atom (cons client nil)))))
      (do (swap! counter dec) (info "dec" @counter)))))

(defn acquire-client ^AsynchronousSocketChannel [^InetSocketAddress remoteaddress]
  (let [pool (from-context :client-pool)]
    (if-let [remote (get @pool remoteaddress)]
      (loop []
        (let [[^AsynchronousSocketChannel head & rest :as all] @remote]
          (if head
            (if (compare-and-set! remote all rest)
              (do
                (swap! acqcounter inc) (let [c @acqcounter] (when (= 0 (mod c 1000000)) (info "acq" c)))
                head)
              (recur))
            (mk-client))))
      (mk-client))))

;; eof
