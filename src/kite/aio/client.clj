(in-ns 'kite.aio)

(import
  [java.io
   IOException]
  [java.net
   InetSocketAddress
   SocketException]
  [java.nio.channels
   AsynchronousSocketChannel
   CompletionHandler]
  [java.util.concurrent
   ScheduledFuture
   TimeoutException])

;; socket-client with pooled client sockets

(defn remote-address [^String hostname-or-ip ^Long port]
  (InetSocketAddress. hostname-or-ip port))

(defn open-client
  ([^InetSocketAddress remoteaddress succ]
   (open-client remoteaddress succ (fn [e] (error "open-client" e))))
  ([^InetSocketAddress remoteaddress succ fail]
   (open-client remoteaddress -1 succ fail))
  ([^InetSocketAddress remoteaddress ^Long timeout succ fail]
   (if-let [client (acquire-client remoteaddress)]
     (if (connected? client)
       (succ client)
       (let [timeout-occurred (when (> timeout 0) (atom false))
             p (promise)
             s (when (> timeout 0)
                 (schedule-once (fn []
                                  (compare-and-set! timeout-occurred false true)
                                  (complete p (failure (TimeoutException. (<< "Could not connect to '~{remoteaddress}' within ~{timeout} ms.")))))
                                timeout))
             handle (fn [v] (if timeout-occurred
                              (when-not @timeout-occurred
                                (.cancel ^ScheduledFuture s true)
                                (complete p v))
                              (complete p v)))
             h (reify CompletionHandler
                 (^void failed [_ ^Throwable e _]
                   (handle (failure e)))
                 (^void completed [_ _ _]
                   (handle (success client))))]
         (on-success-or-failure (->future p) succ fail)
         (.connect ^AsynchronousSocketChannel client remoteaddress nil h)))
     (fail (SocketException. "Could not create a new socket or retrieve one from a pool.")))))

(defn close-client [^AsynchronousSocketChannel client]
  (release-client client))

;; eof
