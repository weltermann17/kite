(in-ns 'kite-test)

(expect 0 (byte-array-index-of (.getBytes "aaaaa") (.getBytes "a")))
(expect 0 (byte-array-index-of (.getBytes "aaaaa") (.getBytes "aa")))
(expect 1 (byte-array-index-of (.getBytes "baaaa") (.getBytes "a")))
(expect 2 (byte-array-index-of (.getBytes "bbaaa") (.getBytes "aa")))
(expect 2 (byte-string-index-of (byte-string (.getBytes "bbaaa")) (.getBytes "aa")))
(expect -1 (byte-array-index-of (.getBytes "bbbaa") (.getBytes "aaa")))
(expect 7 (byte-array-index-of (.getBytes "aaaaabbaaa") (.getBytes "aaa") 4))
(expect -1 (byte-array-index-of (.getBytes "aaaaabbaaa") (.getBytes "aaa") 4 9))
(expect 7 (byte-array-index-of (.getBytes "aaaaabbaaa") (.getBytes "aaa") 4 10))
(expect [(byte-string (.getBytes "aaa")) (byte-string (.getBytes "aabbaaa"))]
        (starts-with (byte-string (.getBytes "aaaaabbaaa")) (.getBytes "aaa")))
(expect nil (starts-with (byte-string (.getBytes "aaaaabbaaa")) (.getBytes "bba")))

;; eof
