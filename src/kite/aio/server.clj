(in-ns 'kite.aio)

;; socket-server

(defn open-server

  "Returns a future. On success the server is passed to the 'succ' callback. "
  ([^InetSocketAddress address]
   (open-server address nil nil))
  ([^InetSocketAddress address succ]
   (open-server address succ (fn [e] (error "server" e))))
  ([^InetSocketAddress address succ fail]
   (let [config (from-context :config)
         backlog (:socket-backlog config)]
     (open-server address backlog succ fail)))
  ([^InetSocketAddress address ^Long backlog succ fail]
   (completable-future
     succ fail
     (let [channel-group (from-context :channel-group)
           config (from-context :config)
           reuse (:socket-reuse-address config)
           rcvbs (Integer/valueOf (int (:socket-receive-buffer-size config)))]
       (doto (AsynchronousServerSocketChannel/open channel-group)
         (.setOption StandardSocketOptions/SO_REUSEADDR reuse)
         (.setOption StandardSocketOptions/SO_RCVBUF rcvbs)
         (.bind address backlog))))))

;; accept connections

(defn accept
  ([^AsynchronousServerSocketChannel server]
   (accept server nil nil))
  ([^AsynchronousServerSocketChannel server succ]
   (accept server succ (fn [e] (when-not (closing-socket-exception? e) (error "accept" e)))))
  ([^AsynchronousServerSocketChannel server succ fail]
   (let [p (promise)
         h (reify CompletionHandler
             (^void failed [_ ^Throwable e _]
               (complete p (failure e)))
             (^void completed [_ socket _]
               (complete p (success (configure-socket ^AsynchronousSocketChannel socket)))))]
     (.accept server nil h)
     (on-complete (->future p) succ fail))))

;; eof
