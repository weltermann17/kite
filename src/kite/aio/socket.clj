(in-ns 'kite.aio)

(import
  [java.io
   EOFException]
  [java.net
   StandardSocketOptions]
  [java.nio
   ByteBuffer]
  [java.nio.channels
   AsynchronousSocketChannel
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
  (reader true))

(defn- default-socket-no-delay []
  (reader true))

(defn- default-socket-read-write-timeout []
  (reader (* 15 1000)))

;; socket

(defn close-or-ignore [^AsynchronousSocketChannel socket]
  "Ignore any exception thrown on calling 'close'."
  (try (.close socket) (catch Throwable _)))

(defn configure-socket [^AsynchronousSocketChannel socket]
  (try
    (let [config (from-context :config)
          keepalive (:socket-keep-alive config)
          nodelay (:socket-no-delay config)
          rcvbs (Integer/valueOf (int (:socket-receive-buffer-size config)))
          sndbs (Integer/valueOf (int (:socket-send-buffer-size config)))]
      (doto socket
        (.setOption StandardSocketOptions/SO_KEEPALIVE keepalive)
        (.setOption StandardSocketOptions/TCP_NODELAY nodelay)
        (.setOption StandardSocketOptions/SO_RCVBUF rcvbs)
        (.setOption StandardSocketOptions/SO_SNDBUF sndbs)
        ))
    (catch Throwable e (close-or-ignore socket) (throw e))))

;; read handling

(def ^:private eof-exception
  (EOFException. "AsynchronousSocketChannel.read returned -1"))

(defn read-socket [^AsynchronousSocketChannel socket ^ByteBuffer buffer ^Long timeout succ fail]
  (let [p (promise)
        r (reify CompletionHandler
            (^void failed [_ ^Throwable e _]
              (close-or-ignore socket)
              (complete p (failure e)))
            (^void completed [this bytesread a]
              (if (> bytesread -1)
                (complete p (success (.flip buffer)))
                (.failed this eof-exception a))))
        f (on-success-or-failure (->future p) succ fail)]
    (.read socket
           buffer
           timeout TimeUnit/MILLISECONDS
           nil
           r)
    f))

;; write handling

(defn write-socket [^AsynchronousSocketChannel socket ^ByteBuffer buffer ^Long timeout succ fail]
  (let [p (promise)
        w (reify CompletionHandler
            (^void failed [_ ^Throwable e _]
              (close-or-ignore socket)
              (complete p (failure e)))
            (^void completed [this _ a]
              (if (> (.remaining buffer) 0)
                (.write socket buffer timeout TimeUnit/MILLISECONDS a this)
                (complete p (success buffer)))))
        f (on-success-or-failure (->future p) succ fail)]
    (.write socket
            buffer
            timeout TimeUnit/MILLISECONDS
            nil
            w)
    f))

;; basic testing

(def default-response
  (let [c 48
        s "HTTP/1.1 200 OK\nConnection: keep-alive\nContent-Type: text/plain\nContent-Length: 4\nDate: Wed, 11 Mar 2015 13:13:24 GMT\n\npong"
        ^String r (reduce str (repeat c s))]
    (.getBytes r)))

;; eof
