(ns indexer-test
  (:require [clojure.test :refer :all]
            [indexer.indexer :refer :all]
            [me.raynes.fs :as fs]))


(def ^:dynamic *src*)
(def ^:dynamic *target*)

(defn temp-files [t]
  (let [src (fs/temp-file "a-file")]
    (with-bindings {#'*src* src
                    #'*target* (target-path "/tmp/index" src)}
      (t)
      (fs/delete *src*)
      (fs/delete *target*))))

(use-fixtures :each temp-files)

(deftest t-linked
  (is (not (linked? *src* *target*)))
  (fs/sym-link *target* *src*)
  (is (= true (linked? *src* *target*))))
