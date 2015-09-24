(in-ns 'kite.string)

(import
  [java.util Arrays]
  [clojure.lang Counted])

;; types

(defprotocol ByteStringFns
  (blank? [_]
    "Returns true if this has a length of 0, has a length of 0 when trimmed,
    is nil or is the nil-bytes-string.")
  (convert-to-byte-array [_]
    "Returns a new byte array from 0 to length with the content of this. Expensive.")
  (convert-to-string [_]
    "Returns the content as a String. Expensive.")
  (split-delimiter [_ ^bytes delimiter]
    "Returns a vector of ByteString with all chunks each of it without the delimiter.")
  (starts-with [_ ^bytes pattern]
    "If true returns a vector of the part that matches the pattern and the remainder.
     Else returns nil. 'pattern' must not be longer than this.")
  (take-until [_ ^bytes delimiter]
    "If the delimiter is found returns a vector of two ByteStrings first with everything
    before the delimiter and second with the remainder, both without the delimiter.
    Else returns a vector of this and nil."))

(extend-type nil
  ByteStringFns
  (blank? [_] true)
  (convert-to-byte-array [_] nil)
  (convert-to-string [_] nil)
  (split-delimiter [_ _] nil)
  (starts-with [_ _] nil)
  (take-until [_ _] nil))

(declare byte-array-index-of byte-string-index-of byte-string nil-byte-string)

(deftype ByteString [^bytes array ^long from ^long to ^long length]
  Counted
  (count [_] length)

  ByteStringFns
  (blank? [_] (= 0 length))
  (convert-to-byte-array [this]
    (Arrays/copyOfRange array from to))
  (convert-to-string [_]
    (String. array from length))
  (split-delimiter [this delimiter]
    (loop [result [] [next rest] (take-until this delimiter)]
      (if (blank? next)
        result
        (recur (conj result next) (take-until rest delimiter)))))
  (starts-with [_ pattern]
    (let [n (count pattern)]
      (when (= n (loop [i from]
                   (if (and (< i n) (= (aget array i) (aget ^bytes pattern i)))
                     (recur (inc i))
                     i)))
        [(byte-string array 0 n) (byte-string array n to)])))
  (take-until [this delimiter]
    (let [i (byte-string-index-of this delimiter)]
      (if (= -1 i)
        [this nil]
        [(byte-string array from i) (byte-string array (+ i (count delimiter)) to)])))

  Object
  (equals [this that]
    (and (satisfies? ByteStringFns that)
         (= length (count that))
         (not= nil (starts-with this (convert-to-byte-array that)))))
  (hashCode [_] (hash array))
  (toString [this] (comment this) (<< "(ByteString array:'~{(convert-to-string this)}' from:~{from} to:~{to})")))

;; fns

(defn ^ByteString byte-string
  "Create a ByteString from a byte-array."
  ([^bytes array]
   (byte-string array 0))
  ([^bytes array ^long from]
   (let [to (count array)] (byte-string array from to (- to from))))
  ([^bytes array ^long from ^long to]
   (byte-string array from to (- to from)))
  ([^bytes array, ^long from ^long to ^long length]
   (ByteString. array from to length)))

(defn byte-string-index-of ^long [^ByteString b ^bytes pattern]
  "Returns the index of 'pattern' within this or -1 if not found."
  (byte-array-index-of ^bytes (.array b) pattern ^long (.from b) ^long (.to b)))

(def ^:constant nil-byte-string (byte-string nil-bytes))

;; eof
