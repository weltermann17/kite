(in-ns 'kite.aio)

;; socket-server

(defn open-server
  "Returns a future. On success the server is passed to the 'succ' callback. "
  ([^Long port succ]
   (open-server port succ (fn [e] (error "server" e))))
  ([^Long port succ fail]
   (let [config (from-context :config)
         backlog (:socket-backlog config)]
     (open-server (InetSocketAddress. port) backlog succ fail)))
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

(defn close-server [^AsynchronousServerSocketChannel server]
  "Not really useful, just for symmetry."
  (close-socket server))

(defn accept
  ([^AsynchronousServerSocketChannel server succ]
   (accept server succ (fn [e] (error "accept" e))))
  ([^AsynchronousServerSocketChannel server succ fail]
   (let [p (promise)
         f (->future p)
         h (reify CompletionHandler
             (^void failed [_ ^Throwable e _]
               (when-not (closing-socket-exception? e)
                 (accept server succ fail))
               (complete p (failure e)))
             (^void completed [_ socket _]
               (complete p (success (configure-socket ^AsynchronousSocketChannel socket)))
               (accept server succ fail)))]
     (on-success-or-failure f succ fail)
     (.accept server nil h)
     f)))

(defn fast-accept [^AsynchronousServerSocketChannel server succ fail]
  "Callback called in completion handler directly."
  (let [h (letfn [(handle [f v]
                          (f v)
                          (accept server succ fail))]
            (reify CompletionHandler
              (^void failed [_ ^Throwable e _]
                (when-not (instance? AsynchronousCloseException e)
                  (handle fail e)))
              (^void completed [_ socket _]
                (handle succ (configure-socket socket)))))]
    (.accept server nil h)))

;; eof
