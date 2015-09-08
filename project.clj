(defproject
  kite "0.1.0"
  :description "A conglomeration from different monad libraries."
  :jar-name "kite.jar"
  :release-tasks [["clean"]
                  ["expectations"]
                  ["check"]
                  ["jar"]]
  :dependencies [
                 ;; clojure
                 [org.clojure/clojure "1.8.0-alpha4"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.incubator "0.1.3"]

                 ;; testing, facades
                 [criterium "0.4.3"]
                 [expectations "2.1.2"]
                 [potemkin "0.4.1"]

                 ;; logging
                 [org.slf4j/slf4j-api "1.7.12"]
                 [org.apache.logging.log4j/log4j-core "2.3"]
                 [org.apache.logging.log4j/log4j-api "2.3"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.3"]
                 [com.lmax/disruptor "3.3.2"]
                 [org.clojure/tools.logging "0.3.1"]

                 ]
  :source-paths ["src"]
  :target-path "target/")

;; eof

