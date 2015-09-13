(in-ns 'kite.aio)

(import
  [java.nio ByteBuffer])

(defn- default-byte-buffer-size []
  (reader (* 64 1024)))

(defn- default-byte-buffer-initial-pool-size []
  (reader (* 0 1024)))

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

(defn- ^ByteBuffer mk-buffer []
  (let [buffersize (from-context :byte-buffer-size)]
    (ByteBuffer/allocateDirect buffersize)))

(defn release-buffer [^ByteBuffer buffer]
  (let [pool (from-context :byte-buffer-pool)]
    (when (nil? pool) (error "rls pool nil"))
    (when (nil? buffer) (error "rls buf nil"))
    (swap! pool conj (.clear buffer))))

(defn ^ByteBuffer acquire-buffer []
  (let [pool (from-context :byte-buffer-pool)]
    (loop []
      (let [[^ByteBuffer head & rest :as all] @pool]
        (if head
          (if (compare-and-set! pool all rest)
            head
            (recur))
          (mk-buffer))
        ))))

(defn ->byte-array [^ByteBuffer buffer]
  "Converts buffer content to a byte-array and also releases the buffer to the buffer-pool."
  (let [a (byte-array (.remaining buffer))]
    (.get buffer a)
    (release-buffer buffer)
    a))

;; eof
