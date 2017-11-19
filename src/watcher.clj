(ns watcher
  (:require [hawk.core :as h]
            [clojure.tools.logging :as log]))

(def state (atom nil))

(def file-filter (fn [_ {:keys [file]}]
                   (re-matches #"\p{Alnum}+.*\.\p{Alnum}+" (.getName file))))

(defn start! [cfg]
  (reset! state
          (h/watch! [{:paths (:source cfg)
              :filter file-filter
              :context (constantly cfg)
                      :handler (:indexer cfg)}]))
  (log/info "Set watch to " @state))

(defn stop! []
  (if-let [watch @state]
    (h/stop! watch)))

