(in-ns 'kite.aio)

(import
  [java.net
   InetSocketAddress
   StandardSocketOptions]
  [java.nio.channels
   AsynchronousServerSocketChannel
   CompletionHandler])

;; socket-server

(defn socket-server
  ([^Long port succ fail]
   (let [config (from-context :config)
         backlog (:socket-backlog config)]
     (socket-server (InetSocketAddress. port) backlog succ fail)))
  ([^InetSocketAddress address ^Long backlog succ fail]
   (let [f (future
             (let [channel-group (from-context :channel-group)
                   config (from-context :config)
                   reuse (:socket-reuse-address config)
                   rcvbs (Integer/valueOf (int (:socket-receive-buffer-size config)))]
               (doto (AsynchronousServerSocketChannel/open channel-group)
                 (.setOption StandardSocketOptions/SO_REUSEADDR reuse)
                 (.setOption StandardSocketOptions/SO_RCVBUF rcvbs)
                 (.bind address backlog))))]
     (on-success-or-failure f succ fail))))

(declare accept)

(def ^:private accept-handler
  (letfn [(handle [a v] (let [[server p succ fail] a]
                          (complete p v) (accept server succ fail)))]
    (reify CompletionHandler
      (^void failed [_ ^Throwable e a]
        (handle a (failure e)))
      (^void completed [_ socket a]
        (handle a (success socket))))))

(defn accept [^AsynchronousServerSocketChannel server succ fail]
  (let [p (promise)
        f (on-success-or-failure (->future p) succ fail)]
    (.accept server [server p succ fail] accept-handler)
    f))

;; eof
