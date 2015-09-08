(in-ns 'kite.aio)

(import
  [java.nio.channels
   AsynchronousChannelGroup]
  [java.util.concurrent
   TimeUnit])

;; channel-group

(defn- default-channel-group []
  (m-do [_ (ask)]
        [:let executor (from-context :executor)]
        [:return (fn [] (AsynchronousChannelGroup/withThreadPool executor))]))

(defn await-channel-group-termination
  ([^AsynchronousChannelGroup channel-group]
   (await-channel-group-termination channel-group Long/MAX_VALUE))
  ([^AsynchronousChannelGroup channel-group ^Long timeout]
   (.awaitTermination
     channel-group
     timeout TimeUnit/MILLISECONDS)))

;; eof
