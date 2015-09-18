(in-ns 'kite.aio)

(import
  [java.io
   EOFException]
  [java.net
   StandardSocketOptions]
  [java.nio.channels
   AsynchronousSocketChannel
   ClosedChannelException
   CompletionHandler]
  [java.util.concurrent
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

;; socket

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

;; common handling

(defn- handle-failed [p ^Throwable e ^ByteBuffer b ^AsynchronousSocketChannel socket]
  (release-buffer b)
  (close-socket socket)
  (when p (when-not (or (instance? ClosedChannelException e) (= "Connection reset by peer" (.getMessage e)))
            (complete p (failure e)))))

(def ^:constant socket-eof-exception
  (EOFException. "AsynchronousSocketChannel.read returned -1"))

;; read handling

(defn read-socket [^AsynchronousSocketChannel socket
                   ^Long timeout
                   succ
                   fail]
  (let [p (promise)
        b (acquire-buffer)
        h (reify CompletionHandler
            (^void failed [_ ^Throwable e _]
              (handle-failed p e b socket))
            (^void completed [_ bytesread _]
              (if (== -1 bytesread)
                (handle-failed p socket-eof-exception b socket)
                (complete p (success (byte-array-from-buffer b))))))]
    (on-success-or-failure (->future p) succ fail)
    (.read socket
           b
           timeout TimeUnit/MILLISECONDS
           nil
           h)))

;; write handling

(defn write-socket [^AsynchronousSocketChannel socket
                    ^bytes bytes
                    ^Long timeout
                    succ
                    fail]
  (let [p (promise)
        b (byte-buffer-from-array bytes)
        h (reify CompletionHandler
            (^void failed [_ ^Throwable e _]
              (handle-failed p e b socket))
            (^void completed [this _ a]
              (if (== 0 (.remaining b))
                (do (release-buffer b)
                    (complete p (success [])))
                (.write socket b timeout TimeUnit/MILLISECONDS a this))
              ))]
    (on-success-or-failure (->future p) succ fail)
    (.write socket
            b
            timeout TimeUnit/MILLISECONDS
            nil
            h)))

;; eof
