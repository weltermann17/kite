(in-ns 'kite.http)

;; status codes

(def ^:constant client-400 (control-exception "400 Bad Request"))
(def ^:constant client-401 (control-exception "401 Unauthorized"))
(def ^:constant client-404 (control-exception "404 Not Found"))
(def ^:constant client-405 (control-exception "405 Method Not Allowed"))
(def ^:constant client-406 (control-exception "406 Not Acceptable"))

;; eof
