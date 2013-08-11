(ns leiningen.s3-sync.file-system-test
  (:require [clojure.test :refer :all]
            [leiningen.s3-sync.file-system :as fs]))

(def hello-file-sync-state {:path "hello.txt" :md5 "09f7e02f1290be211da707a266f153b3"})
(def world-file-sync-state {:path "world.txt" :md5 "52f83ff6877e42f613bcd2444c22528c"})
(def subcontinent-file-sync-state {:path "sub/continent.txt" :md5 "4e3a9a3106fb00b721dc8c2cfac98351"})

(def example-sync-state
  #{hello-file-sync-state
    world-file-sync-state
    subcontinent-file-sync-state})

(deftest resolving-the-md5-of-each-file-in-a-dir
  (let [local-dir-details (:local-file-details (fs/analyse-local-directory "test/example"))]
    (is (= 3 (count local-dir-details)))
    (is (= example-sync-state local-dir-details))))

