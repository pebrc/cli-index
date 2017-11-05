(set-env!
 :source-paths #{"test"}
 :resource-paths #{"src"}
 :dependencies '[[me.raynes/fs "1.4.6"]
                 [hawk "0.2.11"]
                 [clojure.java-time "0.3.0"]
                 [adzerk/boot-test "1.2.0" :scope "test"] 
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojure/tools.cli "0.3.5"]])

(require '[adzerk.boot-test :refer :all])
