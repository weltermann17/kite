(in-ns 'kite.http)

(import [kite.string ByteString])

;; parser constants

(def ^:constant ^bytes end-of-line (.getBytes "\r\n"))

(def ^:constant ^bytes end-of-headers (.getBytes "\r\n\r\n"))

(def ^:constant ^bytes space (.getBytes " "))

(def ^:constant ^bytes tab (.getBytes "\t"))

(def ^:constant ^bytes slash (.getBytes "/"))

(def ^:constant ^bytes colon-delimiter (.getBytes ": "))

;; constant byte-strings

(def ^:constant ^ByteString method-get (byte-string (.getBytes "GET")))

(def ^:constant ^ByteString method-head (byte-string (.getBytes "HEAD")))

(def ^:constant ^ByteString method-put (byte-string (.getBytes "PUT")))

(def ^:constant ^ByteString method-post (byte-string (.getBytes "POST")))

(def ^:constant ^ByteString method-delete (byte-string (.getBytes "DELETE")))

(def ^:constant ^ByteString method-options (byte-string (.getBytes "OPTIONS")))

(def ^:constant ^ByteString method-trace (byte-string (.getBytes "TRACE")))

(def ^:constant ^ByteString http-1-0 (byte-string (.getBytes "HTTP/1.0")))

(def ^:constant ^ByteString http-1-1 (byte-string (.getBytes "HTTP/1.1")))

; (def ^:constant ^ByteString http-2-0 (byte-string (.getBytes "HTTP/2.0")))

;; eof
