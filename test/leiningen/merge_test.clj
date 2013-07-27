(ns leiningen.merge-test
  (:require [clojure.test :refer :all]
            [leiningen.merge :as m]))

(def hello-file-sync-state {:path "hello.txt" :md5 "09f7e02f1290be211da707a266f153b3"})
(def world-file-sync-state {:path "world.txt" :md5 "52f83ff6877e42f613bcd2444c22528c"})
(def subcontinent-file-sync-state {:path "sub/continent.txt" :md5 "4e3a9a3106fb00b721dc8c2cfac98351"})

(def example-sync-state
  #{hello-file-sync-state
    world-file-sync-state
    subcontinent-file-sync-state})

(deftest local-wins-merging-strategy

  (testing "when all files match there are no deltas"
    (let [deltas (m/generate-deltas example-sync-state example-sync-state)]
      (is (empty? deltas))))

  (testing "when none of the files exist on s3 there is an upload delta for each"
    (let [expected-deltas #{[:upload hello-file-sync-state]
                            [:upload world-file-sync-state]
                            [:upload subcontinent-file-sync-state]} 
          deltas (m/generate-deltas example-sync-state #{})]
      (is (= expected-deltas deltas))))

  (testing "when some of the files exist on s3 there is only a delta for the missing ones"
    (let [expected-deltas #{[:upload world-file-sync-state]
                            [:upload subcontinent-file-sync-state]}
          remote-sync-state #{hello-file-sync-state} 
          actual-deltas (m/generate-deltas example-sync-state remote-sync-state)]
      (is (= expected-deltas actual-deltas))))

  (testing "when some of the local have a changed hash a delta is create for each"
    (let [expected-deltas #{[:upload hello-file-sync-state]}
          remote-sync-state #{(assoc hello-file-sync-state :md5 "out-of-date")
                              world-file-sync-state
                              subcontinent-file-sync-state}
          actual-deltas (m/generate-deltas example-sync-state remote-sync-state)]
      (is (= expected-deltas actual-deltas)))))
