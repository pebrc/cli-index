(ns indexer.indexer
  (:require [indexer.parse :refer :all]
            [java-time :refer [year month]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.core.async :refer [chan go-loop timeout <! >!!]]))

(defn indexable? [file]
  (re-matches #"\p{Alnum}+.*\.\p{Alnum}+" (.getName file)))

(defn target-path [t f]
  (if-let [d (parse-date f)]
    (str t "/" (year d) "/" (.getValue (month d)) "/" (.getName f))))


(defn linked? [src lnk]
  (when (fs/link? lnk)
    (let [target (fs/read-sym-link lnk)]
      (= src target))))

(defn link [src target]
  (when (and  target (not (linked? src target)))
    (io/make-parents target)
    (when (fs/link? target)
      (fs/delete target))
    (fs/sym-link target src)))

(defn index [evts]
  (go-loop []
    (let [{:keys [file target]} (<! evts)]
      (when (indexable? file)
        (let [t (target-path target file)]
          (log/trace "linking " file " to " t)
          (link file t)))
      (recur))))

(defn handler [ctx e]
  (let [f (:file e)
        t (:target ctx)
        c (:in ctx)
        index-fn #(>!! c (assoc e :target t))
        _ (log/debug ctx e)]
    (case (:kind e)
      :create (do (index-fn)  ctx)
      :modify (do (index-fn) ctx)
      :delete (do (fs/delete (target-path t f)) ctx))))

