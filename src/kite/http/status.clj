(in-ns 'kite.http)

;; status codes as control-exceptions

(def ^:constant info-100 (control-exception "100 Continue"))
(def ^:constant info-101 (control-exception "101 Switching Protocols"))
(def ^:constant info-102 (control-exception "102 Processing"))

(def ^:constant success-200 (control-exception "200 OK"))
(def ^:constant success-201 (control-exception "201 Created"))
(def ^:constant success-202 (control-exception "202 Accepted"))
(def ^:constant success-204 (control-exception "204 No Content"))
(def ^:constant success-206 (control-exception "206 Partial Content"))

(def ^:constant client-400 (control-exception "400 Bad Request"))
(def ^:constant client-401 (control-exception "401 Unauthorized"))
(def ^:constant client-404 (control-exception "404 Not Found"))
(def ^:constant client-405 (control-exception "405 Method Not Allowed"))
(def ^:constant client-406 (control-exception "406 Not Acceptable"))

(def ^:constant server-500 (control-exception "500 Internal Server Error"))
(def ^:constant server-501 (control-exception "501 Not Implemented"))
(def ^:constant server-503 (control-exception "503 Service Unavailable"))
(def ^:constant server-505 (control-exception "505 HTTP Version Not Supported"))

;; eof
