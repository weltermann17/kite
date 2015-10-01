(in-ns 'kite-test)

(import [kite.string ByteString])

(def ^:private ^:constant ^ByteString response
  (let [c 48
        s "HTTP/1.1 200 OK\nConnection: keep-alive\nContent-Type: text/plain\nContent-Length: 4\nDate: Wed, 11 Mar 2015 13:13:24 GMT\n\npong"
        r (reduce str (repeat c s))]
    (byte-string (.getBytes ^String r))))

(defn- err [prefix ^Throwable e]
  (when-not (harmless-socket-exception? e) (error prefix (type e) (.getMessage e) e)))

(defn- mk-err [prefix] (partial err prefix))

(def ^:private read-e (mk-err :read))

(def ^:private write-e (mk-err :write))

(def ^:private ctx
  (-> {}
      (add-execution-context {})
      (add-aio-context {})))

(def ^:private port 3001)

(expect
  nil
  (with-context
    ctx
    (open-server
      port
      (fn [server]
        (accept
          server
          (fn [socket]
            (letfn [(write-h [_]
                             (read-socket socket
                                          read-h
                                          read-e))
                    (read-h [^ByteString b]
                            (parse-requests b
                                            (fn [_]
                                              (write-socket socket
                                                            response
                                                            write-h
                                                            write-e))
                                            (fn [e] (error e))))]
              (write-h empty-byte-array)))
          (mk-err :accept))
        (info server)
        (open-client "localhost" 3001 500
                     (fn [c]
                       ;(info "Connected" c)
                       (close-client c)
                       (doall
                         (for [i (range 1)] (future
                                              (info "start loop" i)
                                              (loop [j 100000000]
                                                (info "j" j)
                                                (open-client "localhost" 3001 500
                                                             (fn [c]
                                                               (info "Connected" i c)
                                                               (Thread/sleep 5000)
                                                               (close-client c))
                                                             (fn [e] (error "Second failed" i e)))
                                                (info "j after open" j)
                                                (if (> j 0) (recur (dec j)) j))))))
                     (fn [e] (error "Connection failed" e))))
      (mk-err :server))
    (await-channel-group-termination (from-context :channel-group) 119000)
    (shutdown-channel-group (from-context :channel-group) 1000)))

;; testing

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
    (with-progress-reporting (bench (stress 10)))))

;; eof
