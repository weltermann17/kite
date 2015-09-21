(in-ns 'kite.aio)

(import
  [clojure.lang Counted])

;; types

(defprotocol ByteStringMethods
  (take-until [_ ^bytes delimiter]
    "Returns a vector of two ByteStrings with the everything before the delimiter and the remainder, both without the delimiter.")
  (split-with-delimiter [_ ^bytes delimiter]
    "Returns a vector of ByteString with all chunks each of it without the delimiter.")
  (->string [_]
    "For debugging."))

(declare byte-string-index-of byte-string nil-byte-string)

(deftype ByteString [^bytes array ^long from ^long to]
  Counted
  (count [_] (- to from))

  ByteStringMethods
  (take-until [this delimiter]
    (let [i (byte-string-index-of this delimiter)]
      (if (= -1 i)
        [this, nil-byte-string]
        [(byte-string array from i) (byte-string array (+ i (count delimiter)) to)])))
  (split-with-delimiter [this delimiter]
    (loop [result [] [next rest] (take-until this delimiter)]
      (if (= 0 (count next))
        result
        (recur (conj result next) (take-until rest delimiter)))))
  (->string [this]
    (String. array from (count this)))

  Object
  (toString [this] (comment this) (<< "(ByteString array:~{(->string this)} from:~{from} to:~{to})")))

;; fns

(defn ^ByteString byte-string
  ([^bytes array]
   (byte-string array 0))
  ([^bytes array ^long from]
   (byte-string array from (count array)))
  ([^bytes array, ^long from ^long to]
   (ByteString. array, from, to)))

(def ^:constant nil-byte-string (byte-string nil-bytes))

;; eof
