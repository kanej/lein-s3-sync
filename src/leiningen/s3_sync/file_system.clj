(ns leiningen.s3-sync.file-system
  (:require [pandect.core :as p]))

(declare relative-path)
(declare path->file-details)

(defn analyse-local-directory
  "Analyse a local directory returnings a set
   of file details describing the relative path
   and md5 checksum of all the files (recursively)
   under the directory."
  [dir-path]
  (let [root-dir (clojure.java.io/file dir-path)
        abs-root-dir-path (.getAbsolutePath root-dir)
        dir-sync-description {:root-dir-path abs-root-dir-path}]
    (->> (file-seq root-dir)
         (filter #(not (.isDirectory %)))
         (map (partial path->file-details abs-root-dir-path))
         (set)
         (assoc dir-sync-description :local-file-details))))

(defn- relative-path [root target]
  (.replaceAll target (str "^" root "/") ""))

(defn- path->file-details [root-path file]
  (let [absolute-path (.getAbsolutePath file)
        rel-path (relative-path root-path absolute-path)
        md5 (p/md5-file absolute-path)]
    {:path rel-path :md5 md5}))
 
