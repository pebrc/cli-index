(ns indexer.reconciler
  (:require [indexer.indexer :as indexer]
            [indexer.watcher :as watcher]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [chan go-loop timeout <!]]))


(def interval 1000)
(def max-interval (* 60 1000))

(defn pick [path]
  (if (and path (fs/directory? path))
    (recur (rand-nth (fs/list-dir path)))
    path))

(defn valid-entry? [path]
  (or
   (nil? path);;empty directory
   (fs/file? path)))


(defn re-index [idx-root src]
  (log/info "reindexing " (fs/list-dir src))
  (doall (->> (fs/list-dir src)
              (filter watcher/indexable?)
              (map (partial indexer/index idx-root)))))

(defn fix [idx-root entry]
  (let [invalid-src-dir (fs/parent (fs/read-sym-link entry))]
    (fs/delete entry)
    (re-index idx-root invalid-src-dir)))

(defn reconcile [idx]
  (go-loop [millis interval]
    (<! (timeout millis))
    (log/info "reconciling " idx " after " (float (/ millis 1000)) " secs")
    (let [e (pick idx)
          v (valid-entry? e)
          delay (if v 1000 -1000)]
      (when-not v
        (fix idx  e))
      (recur (max interval (min max-interval (+ delay  millis)))))))
