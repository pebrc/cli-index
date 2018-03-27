(set-env!
 :source-paths #{"test"}
 :resource-paths #{"src"}
 :dependencies '[[me.raynes/fs "1.4.6"]
                 [hawk "0.2.11"]
                 [clojure.java-time "0.3.0"]
                 [adzerk/boot-test "1.2.0" :scope "test"] 
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.4.474"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [org.slf4j/jul-to-slf4j "1.7.21"]])

(require '[adzerk.boot-test :refer :all]
         '[indexer.cli :as cli] )


(deftask build
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
   (aot :all true)
   (uber)
   (jar :file "project.jar" :main 'indexer.cli)
   (sift :include #{#"project.jar"})
   (target)))

(deftask run
  [a args ARGS str "The arguments to pass the program"]
  (with-pass-thru _
     (apply cli/-main (clojure.string/split args #" "))))
