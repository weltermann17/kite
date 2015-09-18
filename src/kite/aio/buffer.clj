(in-ns 'kite.aio)

(import
  [java.nio ByteBuffer])

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

(defn- ^ByteBuffer mk-buffer []
  (ByteBuffer/allocateDirect (from-context :byte-buffer-size)))

(defn release-buffer [^ByteBuffer buffer]
  (let [pool (from-context :byte-buffer-pool)]
    (swap! pool conj (.clear buffer))))

(defn ^ByteBuffer acquire-buffer []
  (let [pool (from-context :byte-buffer-pool)]
    (when (nil? pool) (error "pool nil" (Thread/currentThread) (all-context)))
    (loop []
      (let [[^ByteBuffer head & rest :as all] @pool]
        (if head
          (if (compare-and-set! pool all rest)
            head
            (recur))
          (mk-buffer))
        ))))

(defn ^bytes byte-array-from-buffer [^ByteBuffer buffer]
  "Converts buffer content to a byte-array and then releases the buffer back to the pool."
  (let [a (byte-array (.remaining (.flip buffer)))]
    (.get buffer a)
    (release-buffer buffer)
    a))

(defn ^ByteBuffer byte-buffer-from-array [^bytes a]
  "Acquires a buffer from the pool and fills it with 'a'."
  (doto (acquire-buffer) (.put a) (.flip)))

;; eof
