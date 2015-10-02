(in-ns 'kite.aio)


(import
  [java.net
   InetSocketAddress
   StandardSocketOptions]
  [java.nio.channels
   AsynchronousCloseException
   AsynchronousServerSocketChannel
   AsynchronousSocketChannel
   ClosedChannelException
   CompletionHandler
   InterruptedByTimeoutException]
  [java.util.concurrent
   ScheduledFuture
   TimeoutException
   TimeUnit])

;; socket options

(defn- default-socket-reuse-address []
  (reader true))

(defn- default-socket-backlog []
  (reader (* 16 1024)))

(defn- default-socket-receive-buffer-size []
  (reader (* 256 1024)))

(defn- default-socket-send-buffer-size []
  (reader (* 256 1024)))

(defn- default-socket-keep-alive []
  (reader false))

(defn- default-socket-no-delay []
  (reader true))

(defn- default-socket-read-write-timeout []
  (reader (* 15 1000)))

;; socket handling

(defn close-socket [^AsynchronousSocketChannel socket]
  "Ignore any exception thrown on calling 'close'."
  (ignore (.close socket)))

(defn configure-socket [^AsynchronousSocketChannel socket]
  (let [config (from-context :config)
        keepalive (:socket-keep-alive config)
        nodelay (:socket-no-delay config)
        rcvbs (Integer/valueOf (int (:socket-receive-buffer-size config)))
        sndbs (Integer/valueOf (int (:socket-send-buffer-size config)))]
    (doto socket
      (.setOption StandardSocketOptions/SO_KEEPALIVE keepalive)
      (.setOption StandardSocketOptions/SO_RCVBUF rcvbs)
      (.setOption StandardSocketOptions/SO_SNDBUF sndbs)
      (.setOption StandardSocketOptions/TCP_NODELAY nodelay))))

;; error handling

(defn- handle-failed [p ^Throwable e ^ByteBuffer b ^AsynchronousSocketChannel socket]
  (release-buffer b)
  (close-socket socket)
  (complete p (failure e)))

(defn closing-socket-exception? [^Throwable e]
  (or
    (instance? AsynchronousCloseException e)
    (instance? ClosedChannelException e)
    (instance? InterruptedByTimeoutException e)
    (= "Connection reset by peer" (.getMessage e))))

;; read handling

(defn read-socket
  "Version of read using futures. It is slower than calling succ/fail directly,
  but doesn't block the io completion thread. Returns a future."
  ([^AsynchronousSocketChannel socket succ]
   (read-socket socket succ (fn [e] (error "read-socket" e))))
  ([^AsynchronousSocketChannel socket succ fail]
   (let [p (promise)
         f (->future p)
         b (acquire-buffer)
         t (from-context :socket-read-write-timeout)
         h (reify CompletionHandler
             (^void failed [_ ^Throwable e _]
               (handle-failed p e b socket))
             (^void completed [_ bytesread _]
               (when (= -1 bytesread) (close-socket socket))
               (complete p (success (byte-string-from-buffer b)))))]
     (on-success-or-failure f succ fail)
     (.read socket
            b
            t TimeUnit/MILLISECONDS
            nil
            h)
     f)))

;; write handling

(defn write-socket
  ([^AsynchronousSocketChannel socket ^ByteString s succ]
   (write-socket socket s succ (fn [e] (error "write-socket" e))))
  ([^AsynchronousSocketChannel socket ^ByteString s succ fail]
   (let [p (promise)
         f (->future p)
         b (byte-buffer-from-string s)
         t (from-context :socket-read-write-timeout)
         h (reify CompletionHandler
             (^void failed [_ ^Throwable e _]
               (handle-failed p e b socket))
             (^void completed [this _ _]
               (if (= 0 (.remaining b))
                 (do (release-buffer b) (complete p (success s)))
                 (.write socket b t TimeUnit/MILLISECONDS nil this))
               ))]
     (on-success-or-failure f succ fail)
     (.write socket
             b
             t TimeUnit/MILLISECONDS
             nil
             h)
     f)))

;; eof
