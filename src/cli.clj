(ns cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [watcher :as watcher]
            [indexer :as indexer]
            [log :as logger])
  (:gen-class))

(def cli-options
  [["-s" "--source DIRECTORY" "source directory, can be specified multiple times"
    :assoc-fn (fn [m k v] (update-in m [k] conj v))]
   ["-t" "--target DIRECTORY" "target directory"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Indexer. Indexes files based on date."
        ""
        "Usage: program-name [options]"
        ""
        "OPTIONS:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "Could not parse options:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn -main
  "CLI entry point"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (try
      (logger/init)
      (log/debug options)
      (log/debug arguments)
      (watcher/start! (assoc options :indexer indexer/handler))
      (while true)
      (catch Exception e (log/error e)))))
