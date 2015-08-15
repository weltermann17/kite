(in-ns 'kite.category)

(defprotocol Category
  (-id [_])
  (-comp [a b]))

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

(defprotocol MonadTransformer
  (-inner [m])
  (-lift [m comp]))


;; eof
