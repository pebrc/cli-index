(ns indexer.reconciler
  (:require
   [indexer.indexer :as indexer]
   [me.raynes.fs :as fs]
   [clojure.tools.logging :as log]
   [clojure.core.async :refer [chan go-loop timeout <! >!!]]))


(def interval 1000)
(def max-interval (* 60 1000))

(defn pick [path]
  (log/trace "pick path " path)
  (if (and path (fs/directory? path))
    (recur (rand-nth (fs/list-dir path)))
    path))

(defn valid-entry? [path]
  (or
   (nil? path);;empty directory
   (fs/file? path)))


(defn re-index [in idx-root src]
  (log/debug "reindexing " src)
  (doall
   (->> (fs/list-dir src)
        (map #(>!! in (assoc {} :target idx-root :file %))))))

(defn fix [in idx-root entry]
  (let [invalid-src-dir (fs/parent (fs/read-sym-link entry))]
    (fs/delete entry)
    (re-index in idx-root invalid-src-dir)))

(defn reconcile-index [idx in]
  (let [e (pick idx)
        v (valid-entry? e)]
    (when-not v
      (fix in idx  e))
    v))

(defn reconcile-src [in target src]
  (if-let [s (pick src)]
    (let [t (indexer/target-path target s)
          v (and t (indexer/indexable? s) (fs/exists? t))
          _ (log/debug "random sample. src " s " target " t  " indexed " v )]
      (when-not v
        (re-index in target (fs/parent s)))
      v)))


(defn reconcile [{:keys [target source in]}]
  (go-loop [millis interval]
    (<! (timeout millis))
    (log/info  "reconciling  after " (float (/ millis 1000)) " secs")
    (let [v1 (->> source
                  (map #(reconcile-src in target %))
                  (some true?))
          v2 (reconcile-index target in)
          delay (if (and v1 v2) 1000 -1000)]
      (recur (max interval (min max-interval (+ delay  millis)))))))
