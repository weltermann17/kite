(in-ns 'kite.aio)

(import
  [java.nio ByteBuffer]
  [kite.string ByteString])

(defn- default-byte-buffer-size []
  (reader (* 64 1024)))

(defn- default-byte-buffer-initial-pool-size []
  (reader (* 1 1024)))

(defn- default-byte-buffer-pool []
  (m-do [poolsize (asks :byte-buffer-initial-pool-size)
         buffersize (asks :byte-buffer-size)]
        [:let
         _ (check-type Long poolsize)
         _ (check-type Long buffersize)
         _ (check-cond (>= poolsize 0))
         _ (check-cond (>= buffersize 512))]
        [:return
         (fn [] (atom (doall (for [_ (range poolsize)]
                               (ByteBuffer/allocateDirect buffersize)))))]))

(defn- mk-buffer ^ByteBuffer []
  (ByteBuffer/allocateDirect (from-context :byte-buffer-size)))

(defn release-buffer [^ByteBuffer buffer]
  "Clears the buffer and releases it back to the pool."
  (let [pool (from-context :byte-buffer-pool)]
    (swap! pool conj (.clear buffer))))

(defn acquire-buffer ^ByteBuffer []
  "Returns a cleared buffer either from the pool or if the pool is empty a newly created one."
  (let [pool (from-context :byte-buffer-pool)]
    (loop []
      (let [[^ByteBuffer head & rest :as all] @pool]
        (if head
          (if (compare-and-set! pool all rest)
            head
            (recur))
          (mk-buffer))
        ))))

;; utilities

(defn byte-string-from-buffer ^ByteString [^ByteBuffer buffer]
  "Converts buffer content to a byte-array and releases the buffer back to the pool."
  (let [a (byte-array (.remaining (.flip buffer)))]
    (release-buffer (.get buffer a))
    (byte-string a)))

(defn byte-buffer-from-string ^ByteBuffer [^ByteString b]
  "Acquires a buffer from the pool and fills it with 'b'. Assumes (but does not assert) that 'capacity' > 'count b'."
  (let [[arr f _ l] (array-from-to-len b)]
    (doto (acquire-buffer) (.put ^bytes arr f l) (.flip))))

;; eof
