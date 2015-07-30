(defproject
  kite "0.1.0"
  :description "A conglomeration from different monad libraries."
  :jar-name "kite.jar"
  :release-tasks [["clean"]
                  ["check"]
                  ["jar"]]
  :dependencies [[org.clojure/clojure "1.8.0-alpha2"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/algo.monads "0.1.5"]
                 [expectations "2.1.2"]]
  :source-paths ["src"]
  :target-path "target/")

;; eof

