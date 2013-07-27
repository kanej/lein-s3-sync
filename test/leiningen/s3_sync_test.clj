(ns leiningen.s3-sync-test
  (:require [clojure.test :refer :all]
            [leiningen.s3-sync :as sync]))

(def expected-dir-details
  #{{:path "hello.txt" :md5 "09f7e02f1290be211da707a266f153b3"} 
    {:path "world.txt" :md5 "52f83ff6877e42f613bcd2444c22528c"} 
    {:path "sub/continent.txt" :md5 "4e3a9a3106fb00b721dc8c2cfac98351"}})

(deftest resolving-the-md5-of-each-file-in-a-dir
  (let [local-dir-details (sync/analyse-dir "test/example")]
    (is (= 3 (count local-dir-details)))
    (is (= expected-dir-details local-dir-details))))

