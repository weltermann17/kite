(in-ns 'kite.aio)

(import
  [java.net
   InetSocketAddress]
  [java.nio.channels
   AsynchronousSocketChannel
   CompletionHandler]
  [java.util.concurrent
   ScheduledFuture
   TimeoutException])

;; socket-client with pooled client sockets

(defn open-client
  "Returns a future. On success 'succ' is called with a connected socket.
  This socket is either newly created or from a pool and newly connected or
  (in the best case) from a pool of already connected sockets. 'fail' is called
  with an exception in any error case including a timeout."
  ([^String hostname ^Long port ^Long timeout succ fail]
   (open-client (InetSocketAddress. hostname port) timeout succ fail))
  ([^InetSocketAddress remoteaddress ^Long timeout succ fail]
   (try
     (assert (> timeout 0))
     (let [client (acquire-client)
           timeout-occurred (atom false)
           p (promise)
           f (->future p)
           s (schedule-once (fn []
                              (compare-and-set! timeout-occurred false true)
                              (complete p (failure (TimeoutException. (<< "Could not connect to '~{remoteaddress}' within ~{timeout} ms.")))))
                            timeout)
           h (letfn
               [(handle [v]
                        (when-not @timeout-occurred
                          (.cancel ^ScheduledFuture s true)
                          (complete p v)))]
               (reify CompletionHandler
                 (^void failed [_ ^Throwable e _]
                   (handle (failure e)))
                 (^void completed [_ _ _]
                   (handle (success client)))))]
       (on-success-or-failure f succ fail)
       (.connect ^AsynchronousSocketChannel client remoteaddress nil h)
       f)
     (catch Throwable e (fail e)))))

(defn close-client [^AsynchronousSocketChannel client]
  (release-client client))

;; eof
