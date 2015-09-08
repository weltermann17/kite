(in-ns 'kite.aio)

(import
  [java.net
   StandardSocketOptions]
  [java.nio
   ByteBuffer]
  [java.nio.channels
   AsynchronousSocketChannel
   CompletionHandler]
  [java.util.concurrent
   TimeUnit])

(def ^:private bufferpool ())

(defn ^ByteBuffer get-byte-buffer [] nil)

(defn release-byte-buffer [^ByteBuffer buffer] nil)

;; eof
