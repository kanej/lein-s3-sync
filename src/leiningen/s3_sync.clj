(ns leiningen.s3-sync
  (:require [pandect.core :as p]
            [leiningen.s3 :as s3]))

;; Get the list of local files
;; For each
;;   check whether the file exists on s3
;;   if not then upload
;;   if it does check whether the md5 hashes match
;;     if not then upload  

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
 
(defn analyse-sync-state [cred dir-path bucket-name]
  (let [local-file-state (analyse-local-directory dir-path)
        file-paths (->> local-file-state
                       (:local-file-details)
                       (map :path))
        s3-file-state (s3/analyse-s3-bucket cred bucket-name file-paths)] 
    (merge local-file-state s3-file-state)))

(defn generate-deltas [local-file-details s3-file-details]
  (let [upload-file-details (clojure.set/difference local-file-details s3-file-details)]
    (set (map #(vector :upload %) upload-file-details))))

