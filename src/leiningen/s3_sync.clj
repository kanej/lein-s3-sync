(ns leiningen.s3-sync
  (:require [pandect.core :as p]))

;; Get the list of local files
;; For each
;;   check whether the file exists on s3
;;   if not then upload
;;   if it does check whether the md5 hashes match
;;     if not then upload  

(declare relative-path)
(declare path->file-details)

(defn analyse-dir
  "Analyse a local directory returnings a set
   of file details describing the relative path
   and md5 checksum of all the files (recursively)
   under the directory."
  [dir-path]
  (let [root-dir (clojure.java.io/file dir-path)
        abs-root-dir-path (.getAbsolutePath root-dir)]
    (->> (file-seq root-dir)
         (filter #(not (.isDirectory %)))
         (map (partial path->file-details abs-root-dir-path))
         (set))))
 
(defn- relative-path [root target]
  (.replaceAll target (str "^" root "/") ""))

(defn- path->file-details [root-path file]
  (let [absolute-path (.getAbsolutePath file)
        rel-path (relative-path root-path absolute-path)
        md5 (p/md5-file absolute-path)]
    {:path rel-path :md5 md5}))
 
