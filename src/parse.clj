(ns parse
  (:require [java-time :as t]
            [me.raynes.fs :as fs])
  (:import [java.util Date]
           [java.io File]
           [java.nio.file Files LinkOption]
           [java.nio.file.attribute BasicFileAttributes]))


(defn parse-legacy-date [s]
  (t/local-date "ddMMyyyy" s))

(defn parse-iso-8601 [s]
  (t/local-date "yyyyMMdd" s))

(defn parse-with-fallback [s]
  (try (parse-iso-8601 s)
       (catch clojure.lang.ExceptionInfo  e (parse-legacy-date s))))

(defn parse-date-str [s]
  (let [[[_ date]] (re-seq #"([0-9]{8})[^0-9]+" s)]
       (parse-with-fallback date)))

(defn creation-time [f]
  (-> (Files/readAttributes
       (.toPath f)
       BasicFileAttributes
       (make-array LinkOption 0))
      (.creationTime)
      (.toInstant)))

(defprotocol DateParser
  (parse-date [s]))


(extend-type String
  DateParser
  (parse-date [s] (parse-date-str s)))

(extend-type File
  DateParser
  (parse-date [s]
    (try (parse-date-str (fs/base-name s))
         (catch Exception e
           (-> s
               creation-time
               (t/local-date "UTC"))))))
