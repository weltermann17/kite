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

(def ^:private address (socket-address "127.0.0.1" 3001))

(def ^:private cc (atom 0))

;; with callbacks

(expect-focused
  nil
  (with-context
    ctx
    (open-server
      address
      (fn [server]
        (letfn
          [(a [] (accept
                   server
                   (fn [client]
                     (letfn
                       [(r [_] (read-socket client w))
                        (w [b] (try-check (write-socket client responsestring r)))]
                       (r nil))
                     (a))))]
          (a))
        (info server)
        (loop [j 0]
          (when (> j 0)
            (open-client
              address
              (fn [client]
                (letfn [(r [_]
                           (read-socket client w)
                           (let [k (swap! cc + 48)] (when (= 0 (mod k (* 48 10000))) (info k))))
                        (w [_] (write-socket client requeststring r))]
                  (w nil))))
            (recur (dec j))))))
    (await-termination (from-context :channel-group) 119000)
    (shutdown-channel-group (from-context :channel-group) 1000)))

;; trying the monadic way

(defn monadic-test []
  (let [group (from-context :channel-group)]
    (m-do [server (open-server address)]
          [:return
           (println server)
           (letfn [(a [] (m-do [client (accept server)]
                               [:return
                                (letfn [(b [] (m-do [s (read-socket client)
                                                     r (parse-requests s)
                                                     _ (write-socket client responsestring)]
                                                    [:return
                                                     (b)]))]
                                  (b))
                                (a)]))]
             (a))
           server])
    (await-termination group 120000)))

(defn m-loop* [f]
  (let [fut (f)]
    (on-complete fut (fn [v] (when (success? v) (println "m-loop" v) (m-loop* f))))))

(defmacro m-loop [body]
  `(m-loop* (fn [] ~body)))

(defn- rw [client]
  (m-do [s (read-socket client)
         r (parse-requests s)
         w (write-socket client responsestring)]
        [:return w]))

(defn- acc [server]
  (m-do [client (accept server)
         _ (m-loop (rw client))]
        [:return client]))

(defn monadic-test2 []
  (let [group (from-context :channel-group)
        f (m-do [server (open-server address)
                 client (m-loop (acc server))]
                [:return server])]
    (await f 1000)
    (println "ff" f)
    (await-termination group 120000)))

(comment false (with-context ctx (monadic-test2)))

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

