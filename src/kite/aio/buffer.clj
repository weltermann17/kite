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
  (doto (acquire-buffer) (.put ^bytes (.array b)) (.flip)))

;; search for a pattern within a byte-array

(def ^:private byte-array-index
  (memoize
    (fn ^longs [^bytes pattern]
      (let [n (count pattern)
            failure (long-array n)]
        (loop [i 1]
          (if (< i n)
            (let [m (aget pattern i)
                  j (loop [j 0]
                      (if (and (> j 0) (not= (aget pattern j) m))
                        (recur (aget failure (dec j)))
                        j))]
              (aset-long failure i (if (= (aget pattern j) m) (inc j) j))
              (recur (inc i)))
            failure))))))

(defn byte-array-index-of
  "Ugly, but 5x faster than 'String/indexOf'."
  (^long [^bytes array ^bytes pattern]
   (byte-array-index-of array pattern 0))
  (^long [^bytes array ^bytes pattern ^long from]
   (byte-array-index-of array pattern from (count array)))
  (^long [^bytes array ^bytes pattern ^long from ^long to]
   (let [len (count pattern)]
     (if (= len 1)
       (let [b ^byte (aget pattern 0)]
         (loop [i from]
           (if (< i to)
             (if (= b ^byte (aget array i))
               i
               (recur (inc i)))
             -1)))
       (let [failure (byte-array-index pattern)]
         (loop [i from k 0]
           (if (< i to)
             (let [j (long (loop [l k]
                             (if (and (> l 0) (not= (aget pattern l) (aget array i)))
                               (recur (aget failure (dec l)))
                               (if (= (aget pattern l) (aget array i)) (inc l) l))))]
               (if (= j len)
                 (inc (- i len))
                 (recur (inc i) j)))
             -1)))))))

(defn byte-string-index-of ^long [^ByteString b ^bytes pattern]
  (byte-array-index-of ^bytes (.array b) pattern ^long (.from b) ^long (.to b)))

;; benchmarks

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(def H (.getBytes "Hello"))
(def data (.getBytes "asldkjfaslkdjfHellolaksjdflaksjdflaksj"))

;(with-progress-reporting (bench (byte-array-index-of data H)))

;; eof
