(in-ns 'kite-test)

(import
  [kite.string ByteString])

(def ^:private ^:constant ^ByteString responsestring
  (let [c 48
        s "HTTP/1.1 200 OK\nConnection: keep-alive\nContent-Type: text/plain\nContent-Length: 4\nDate: Wed, 11 Mar 2015 13:13:24 GMT\n\npong"
        r (reduce str (repeat c s))]
    (byte-string (.getBytes ^String r))))

(def ^:private ^:constant ^ByteString requeststring
  (byte-string (.getBytes "GET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\nGET /ping HTTP/1.1\r\nHost: 127.0.0.1:3001\r\n\r\n")))

(def ^:private ctx
  (-> {}
      (add-execution-context {})
      (add-aio-context {})))

(def ^:private port 3001)

(def ^:private cc (atom 0))

(expect
  nil
  (with-context
    ctx
    (open-server
      port
      (fn [server]
        (accept
          server
          (fn [client]
            (letfn
              [(r [_] (read-socket client w))
               (w [b] (parse-requests b (fn [_] (write-socket client responsestring r))))]
              (r nil))))
        (info server)
        ;(schedule-once 5000 (fn [] (close-server server) (warn server)))
        (let [remote (remote-address "localhost" port)]
          (loop [j 200]
            (when (> j 0)
              (open-client
                remote
                (fn [client]
                  (letfn [(r [_]
                             (read-socket client w)
                             (let [k (swap! cc + 48)] (when (= 0 (mod k 1000000)) (info k))))
                          (w [_] (write-socket client requeststring r))]
                    (w nil))))
              (recur (dec j)))))))
    (await-channel-group-termination (from-context :channel-group) 119000)
    (shutdown-channel-group (from-context :channel-group) 1000)))

;; testing

(comment
  (require
    '[criterium.core :refer [bench quick-bench with-progress-reporting]])

  (defn stress-pool [^long n]
    (loop [i n]
      (if (> i 0)
        (do
          (let [b (acquire-buffer)] (.clear b) (release-buffer b))
          (recur (dec i)))
        i)))

  (defn stress [^long n]
    (await
      (first-success
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n))
        (future (stress-pool n)))
      100))

  (comment
    nil
    (with-context
      ctx
      (info (stress 1000))
      (with-progress-reporting (bench (stress 10))))))

;; eof

