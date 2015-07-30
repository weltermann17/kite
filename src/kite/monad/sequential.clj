(in-ns 'kite.monad)

(import
  [clojure.lang Sequential])

(extend-type Sequential
  Functor
  (-fmap [v f] (into [] (map f v)))

  Pure
  (-pure [_ v] [v])

  Applicative
  (-apply [f v] (mapcat #(map % v) f))

  Monad
  (-bind [m f] (mapcat f m))

  Monoid
  (-zero [_] [])
  (-plus [a b] (concat a b))

  MonoidSum
  (-sum [a as] (apply concat a as)))

;; eof
