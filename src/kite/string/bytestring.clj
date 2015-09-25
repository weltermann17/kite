(in-ns 'kite.string)

(import
  [java.util Arrays]
  [clojure.lang Counted])

;; types

(defprotocol ByteStringFns
  (array-from-to-len [_]
    "Returns a vector with these fields.")
  (blank? [_]
    "Returns true if this has a length of 0, has a length of 0 when trimmed,
    is nil or is the nil-bytes-string.")
  (convert-to-byte-array [_]
    "Returns a new byte array from 0 to length with the content of this, this is fairly expensive.")
  (convert-to-string [_]
    "Returns the content as a String, this is fairly expensive.")
  (split-delimiter [_ ^bytes delimiter]
    "Returns a vector of ByteString with all chunks each of it without the delimiter.")
  (starts-with [_ ^bytes pattern]
    "If true returns the remainder of the ByteString else nil. 'pattern' must be longer than 0 but not longer than this.")
  (take-until [_ ^bytes delimiter]
    "If the delimiter is found returns a vector of two ByteStrings first with everything
    until and including the delimiter and second with the remainder.
    If not returns a vector of this and the empty-byte-string.")
  (take-with [_ ^bytes delimiter]
    "Like 'take-until' but including the delimiter if found."))

(declare byte-string-index-of
         byte-string
         empty-byte-string)

(deftype ByteString [^bytes array ^long from ^long to]
  Counted
  (count [_] (- to from))

  ByteStringFns
  (array-from-to-len [_]
    [array from to (- to from)])
  (blank? [_]
    (= to from))
  (convert-to-byte-array [_]
    (Arrays/copyOfRange array from to))
  (convert-to-string [_]
    (String. array from (- to from)))
  (split-delimiter [this delimiter]
    (loop [result [] [next rest] (take-until this delimiter)]
      (if (blank? next)
        (if (blank? rest)
          result
          (recur result (take-until rest delimiter)))
        (recur (conj result next) (take-until rest delimiter)))))
  (starts-with [_ pattern]
    (when pattern
      (let [[parr f t len] (array-from-to-len pattern)]
        (when (= (loop [i from j f]
                   (if (and (< j t) (< i to) (= (aget array i) (aget ^bytes parr j)))
                     (recur (inc i) (inc j))
                     j))
                 len)
          (byte-string array (+ from len) to)))))
  (take-until [this delimiter]
    (let [i (byte-string-index-of this delimiter)]
      (if (= -1 i)
        [this empty-byte-string]
        [(byte-string array from i) (byte-string array (+ i (count delimiter)) to)])))
  (take-with [this delimiter]
    (let [i (byte-string-index-of this delimiter)]
      (if (= -1 i)
        [this empty-byte-string]
        (let [pos (+ i (count delimiter))]
          [(byte-string array from pos) (byte-string array pos to)]))))

  Object
  (equals [this that]
    (and (satisfies? ByteStringFns that)
         (= (count this) (count that))
         ((starts-with this that))))
  (hashCode [_] (hash array))
  (toString [this] (comment this) (<< "(ByteString array '~{(convert-to-string this)}' from ~{from} to ~{to} length ~{(count this)})")))

;; fns

(defn byte-string
  "Create a ByteString from a byte-array."
  (^ByteString [^bytes array]
   (byte-string array 0))
  (^ByteString [^bytes array ^long from]
   (byte-string array from (count array)))
  (^ByteString [^bytes array, ^long from ^long to]
   (ByteString. array from to)))

(defn byte-string-index-of ^long [^ByteString b ^bytes pattern]
  "Returns the index of 'pattern' within this or -1 if not found."
  (byte-array-index-of ^bytes (.array b) pattern ^long (.from b) ^long (.to b)))

;; constants

(def ^:constant empty-byte-string (byte-string empty-byte-array))

;; eof
