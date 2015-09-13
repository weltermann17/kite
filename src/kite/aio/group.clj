(in-ns 'kite.aio)

(import
  [java.nio.channels
   AsynchronousChannelGroup]
  [java.util.concurrent
   ExecutorService
   TimeUnit])

;; channel-group

(defn- default-channel-group []
  (m-do [_ (ask)]
        [:return (fn [^ExecutorService executor]
                   (AsynchronousChannelGroup/withThreadPool executor))]))

(defn await-channel-group-termination
  ([^AsynchronousChannelGroup channel-group]
   (await-channel-group-termination channel-group Long/MAX_VALUE))
  ([^AsynchronousChannelGroup channel-group ^Long timeout]
   (.awaitTermination
     channel-group
     timeout TimeUnit/MILLISECONDS)))

(defn shutdown-channel-group
  ([^AsynchronousChannelGroup channel-group]
   (.shutdownNow channel-group))
  ([^AsynchronousChannelGroup channel-group ^Long timeout]
   (.shutdown channel-group)
   (Thread/sleep timeout)))

;; eof
