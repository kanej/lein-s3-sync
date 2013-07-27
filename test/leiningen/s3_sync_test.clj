(ns leiningen.s3-sync-test
  (require [clojure.test :refer :all]))

;; Get the list of local files
;; For each
;;   check whether the file exists on s3
;;   if not then upload
;;   if it does check whether the md5 hashes match
;;     if not then upload  

(defn relative-path [root target]
  (.replaceAll target (str "^" root "/") ""))

(defn analyse [dir]
  (let [root-dir (clojure.java.io/file dir)
        root-path (.getAbsolutePath root-dir)]
    (->> dir
        (clojure.java.io/file)
        (file-seq)
        (filter #(not (.isDirectory %)))
        (map #(.getAbsolutePath %))
        (map (partial relative-path root-path))
        (map (partial hash-map :path))
        (set))))

(def expected-dir-details
  #{{:path "hello.txt"} 
    {:path "world.txt"} 
    {:path "sub/continent.txt"}})

(deftest resolving-the-md5-of-each-file-in-a-dir
  (let [local-dir-details (analyse "test/example")]
    (is (= 3 (count local-dir-details)))
    (is (= expected-dir-details local-dir-details))))

