(ns parse-test
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [indexer.parse :refer :all]
            [java-time :as t]))


(deftest t-parse-date  
  (are [x y] (= x (parse-date y))
    (t/local-date 2017 10 1) "MTA20171001.pdf"
    (t/local-date 2017 9 23) "SUS20170923b.pdf"
    (t/local-date 1782 1 11) "W17820111_2.pdf"
    (t/local-date 2017 10 8) "A08102017.pdf"    
    (t/local-date) (fs/temp-file "a-file")))

