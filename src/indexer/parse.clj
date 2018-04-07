(ns indexer.parse
  (:require [java-time :as t]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log])
  (:import [java.util Date]
           [java.io File]
           [java.nio.file Files LinkOption]
           [java.nio.file.attribute BasicFileAttributes]))


(defn local-date [x y]
  (when (and x y)
    (t/local-date x y)))

(defn parse-legacy-date [s]
  (local-date "ddMMyyyy" s))

(defn parse-iso-8601 [s]
  (local-date "yyyyMMdd" s))

(defn parse-with-fallback [s]
  (try (parse-iso-8601 s)
       (catch clojure.lang.ExceptionInfo e
         (parse-legacy-date s))))

(defn parse-date-str [s]
  (let [[[_ date]] (re-seq #"[^0-9]([0-9]{8})[^0-9]+" s)]
       (parse-with-fallback date)))

(defn creation-time [f]
  (try (-> (Files/readAttributes
            (.toPath f)
            BasicFileAttributes
            (make-array LinkOption 0))
           (.creationTime)
           (.toInstant))
       (catch Exception e
         (log/warn "Could not parse date:" (:type e) (:message e))
         nil)))


(defprotocol DateParser
  (parse-date [s]))


(extend-type String
  DateParser
  (parse-date [s] (parse-date-str s)))

(extend-type File
  DateParser
  (parse-date [s]
    (or (parse-date-str (fs/base-name s))
         (-> s
             creation-time
             (local-date "UTC")))))
