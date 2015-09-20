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
  "Clears the buffer and releases it back to the pool."
  (let [pool (from-context :byte-buffer-pool)]
    (swap! pool conj (.clear buffer))))

(defn ^ByteBuffer acquire-buffer []
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

(defn ^bytes byte-array-from-buffer [^ByteBuffer buffer]
  "Converts buffer content to a byte-array and releases the buffer back to the pool."
  (let [a (byte-array (.remaining (.flip buffer)))]
    (release-buffer (.get buffer a)) a))

(defn ^ByteBuffer byte-buffer-from-array [^bytes a]
  "Acquires a buffer from the pool and fills it with 'a'."
  (doto (acquire-buffer) (.put a) (.flip)))

(defn- byte-array-compute-failure [^bytes pattern]
  "Very much 'java-style'."
  (let [n (count pattern) failure (long-array n)]
    (loop [i 1]
      (if (< i n)
        (let [j (loop [j 0]
                  (if (and (> j 0) (not= (aget pattern j) (aget pattern i)))
                    (recur (aget failure (- j 1))) j))]
          (aset-long failure i
                     (if (= (aget pattern j) (aget pattern i)) (inc j) j))
          (recur (inc i)))
        failure))))

(defn ^Long byte-array-index-of
  ([^bytes array ^bytes pattern]
   (byte-array-index-of array pattern 0))
  ([^bytes array ^bytes pattern ^Long from]
   (byte-array-index-of array pattern from (count array)))
  ([^bytes array ^bytes pattern ^Long from ^Long to]
   (let [p (count pattern)
         ^longs failure (byte-array-compute-failure pattern)]
     (loop [i from k 0]
       (if (< i to)
         (let [j (loop [j k]
                   (if (and (> j 0) (not= (aget pattern j) (aget array i)))
                     (recur (aget failure (- j 1)))
                     (if (= (aget pattern j) (aget array i)) (inc j) j)))]
           (if (= j p)
             (inc (- i p))
             (recur (inc i) (long j))))
         -1)))))

;; eof
