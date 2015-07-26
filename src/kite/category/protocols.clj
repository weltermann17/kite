(in-ns 'kite.category)

(defprotocol Functor
  (-fmap [v f]))

(defprotocol Pure
  (-pure [_ v]))

(defprotocol Applicative
  (-apply [a b]))

(defprotocol Monad
  (-bind [m f]))

(defprotocol Monoid
  (-zero [_])
  (-plus [a b]))

(defprotocol MonoidSum
  (-sum [a as]))

;; eof
