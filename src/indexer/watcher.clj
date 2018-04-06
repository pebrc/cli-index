(ns indexer.watcher
  (:require [hawk.core :as h]
            [clojure.tools.logging :as log]))

(def state (atom nil))


(defn start! [cfg]
  (reset! state
          (h/watch! [{:paths (:source cfg)
              :context (constantly cfg)
                      :handler (:indexer cfg)}]))
  (log/info "Set watch to " @state))

(defn stop! []
  (if-let [watch @state]
    (h/stop! watch)))

