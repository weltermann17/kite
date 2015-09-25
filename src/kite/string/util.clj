(in-ns 'kite.string)

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
  "Implementation of Knuth/Morris/Pratt algorithm except for pattern lengths 1, 2 and 4."
  (^long [^bytes array ^bytes pattern]
   (byte-array-index-of array pattern 0))
  (^long [^bytes array ^bytes pattern ^long from]
   (byte-array-index-of array pattern from (count array)))
  (^long [^bytes array ^bytes pattern ^long from ^long to]
   (let [len (count pattern)]
     (if (= len 1)
       (let [a ^byte (aget pattern 0)]
         (loop [i from]
           (if (< i to)
             (if (= a ^byte (aget array i))
               i
               (recur (inc i)))
             -1)))
       (if (= len 2)
         (let [a ^byte (aget pattern 0)
               b ^byte (aget pattern 1)
               n (dec to)]
           (loop [i from]
             (if (< i n)
               (if (and (= a ^byte (aget array i))
                        (= b ^byte (aget array (inc i))))
                 i
                 (recur (inc i)))
               -1)))
         (if (= len 3)
           (let [a ^byte (aget pattern 0)
                 b ^byte (aget pattern 1)
                 c ^byte (aget pattern 2)
                 n (- to 2)]
             (loop [i from]
               (if (< i n)
                 (if (and (= a ^byte (aget array i))
                          (= b ^byte (aget array (inc i)))
                          (= c ^byte (aget array (+ i 2))))
                   i
                   (recur (inc i)))
                 -1)))
           (if (= len 4)
             (let [a ^byte (aget pattern 0)
                   b ^byte (aget pattern 1)
                   c ^byte (aget pattern 2)
                   d ^byte (aget pattern 3)
                   n (- to 3)]
               (loop [i from]
                 (if (< i n)
                   (if (and (= a ^byte (aget array i))
                            (= b ^byte (aget array (inc i)))
                            (= c ^byte (aget array (+ i 2)))
                            (= d ^byte (aget array (+ i 3))))
                     i
                     (recur (inc i)))
                   -1)))
             (let [failure ^longs (byte-array-index pattern)]
               (loop [i from k 0]
                 (if (< i to)
                   (let [m (aget array i)
                         j (long (loop [l k]
                                   (let [c (not= (aget pattern l) m)]
                                     (if (and (> l 0) c)
                                       (recur (aget failure (dec l)))
                                       (if c l (inc l))))))]
                     (if (= j len)
                       (inc (- i len))
                       (recur (inc i) j)))
                   -1))))))))))

;; benchmarks

(require
  '[criterium.core :refer [bench quick-bench with-progress-reporting]])

(def H (.getBytes "Hello"))
(def data (.getBytes "asldkjfaslkdjfHellolaksjdflaksjdflaksj"))

; (with-progress-reporting (bench (byte-array-index-of data H)))

;; eof
