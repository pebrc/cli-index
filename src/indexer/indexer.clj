(ns indexer.indexer
  (:require [indexer.parse :refer :all]
            [java-time :refer [year month]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]))


(defn target-path [t f]
  (let [d (parse-date f)]
    (str t "/" (year d) "/" (.getValue (month d)) "/" (.getName f))))

(defn link [src target]
  (io/make-parents target)
  (when (fs/exists? target)
    (fs/delete target))
  (fs/sym-link target src))

(defn index [index-root src]
  (log/debug "linking " src " to " index-root)
  (link src (target-path index-root src)))

(defn handler [ctx e]
  (let [f (:file e)
        t (:target ctx)
        _ (log/debug ctx e)]
    (case (:kind e)
      :create (do (index t f) ctx)
      :modify (do (index t f) ctx)
      :delete (do (fs/delete (target-path t f)) ctx))))

