(ns indexer.indexer
  (:require [indexer.parse :refer :all]
            [java-time :refer [year month]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]))


(defn target-path [t f]
  (let [d (parse-date f)]
    (str t "/" (year d) "/" (month d) "/" (.getName f))))

(defn link [src target]
  (io/make-parents target)
  (when (fs/exists? target)
    (fs/delete target))
  (fs/sym-link target src))

(defn handler [ctx e]
  (let [f (:file e)
        t (:target ctx)
        _ (log/debug ctx e)]
    (case (:kind e)
      :create (do (link f (target-path t f)) ctx)
      :modify (do (link f (target-path t f)) ctx)
      :delete (do (fs/delete (target-path t f)) ctx))))

