(in-ns 'kite.aio)

;; socket-server

(defn open-server
  "Returns a future. On success the server is passed to the 'succ' callback. "
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
  (.close server))

(defn accept [^AsynchronousServerSocketChannel server succ fail]
  "Returns a future."
  (let [p (promise)
        f (->future p)
        h (letfn [(handle [v]
                          (complete p v)
                          (accept server succ fail))]
            (reify CompletionHandler
              (^void failed [_ ^Throwable e _]
                (handle (failure e)))
              (^void completed [_ socket _]
                (handle (success (configure-socket ^AsynchronousSocketChannel socket))))))]
    (on-success-or-failure f succ fail)
    (.accept server nil h)
    f))

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
