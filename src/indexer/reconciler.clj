(ns indexer.reconciler
  (:require 
   [me.raynes.fs :as fs]
   [clojure.tools.logging :as log]
   [clojure.core.async :refer [chan go-loop timeout <! >!!]]))


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


(defn re-index [in idx-root src]
  (log/info "reindexing " (fs/list-dir src))
  (doall
   (->> (fs/list-dir src)
        (map #(>!! in (assoc {} :target idx-root :file %))))))

(defn fix [in idx-root entry]
  (let [invalid-src-dir (fs/parent (fs/read-sym-link entry))]
    (fs/delete entry)
    (re-index in idx-root invalid-src-dir)))

(defn reconcile [idx in]
  (go-loop [millis interval]
    (<! (timeout millis))
    (log/info "reconciling " idx " after " (float (/ millis 1000)) " secs")
    (let [e (pick idx)
          v (valid-entry? e)
          delay (if v 1000 -1000)]
      (when-not v
        (fix in idx  e))
      (recur (max interval (min max-interval (+ delay  millis)))))))
