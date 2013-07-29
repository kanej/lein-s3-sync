(ns leiningen.s3-sync
  (:require [pandect.core :as p]
            [leiningen.s3 :as s3]
            [leiningen.merge :as m]))

(declare sync-to-s3)

(defn s3-sync [{config :s3-sync} & keys]
  (let [cred (select-keys config [:access-key :secret-key])
        dir-path (:local-dir config)
        bucket-name (:bucket config)]
    (print (str "Syncing bucket " bucket-name " with directory " dir-path))
    (sync-to-s3 cred dir-path bucket-name)
    (flush)))

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

(defn calculate-deltas-from [sync-state]
  (let [local-file-details (:local-file-details sync-state)
        s3-file-details (:remote-file-details sync-state)
        deltas (m/generate-deltas local-file-details s3-file-details)]
    (assoc sync-state :deltas deltas)))

(defn resolve-full-path [root-path rel-path]
  (let [root (clojure.java.io/file root-path)
        combined (clojure.java.io/file root rel-path)
        abs-path (.getAbsolutePath combined)]
    abs-path))

(defn push-changes-to-s3 [cred sync-state]
  (let [local-root-path (:root-dir-path sync-state)
        bucket-name (:bucket-name sync-state)]
    (loop [deltas (:deltas sync-state)]
      (if (not (empty? deltas)) 
        (let [[op {rel-path :path}] (first deltas)]
          (print "  " rel-path "uploading ...")
          (s3/put-file 
            cred
            bucket-name
            rel-path
            (resolve-full-path local-root-path rel-path))
          (println "\r  " rel-path "done.        ")
          (recur (rest deltas)))))
    sync-state))

(defn- print-sync-state [{:keys [deltas] :as sync-state}]
  (cond
    (empty? deltas) (println "\rThere are no local changes to push.                      ")
    (= 1 (count deltas)) (println "\nThere is 1 local file change to upload:")
    :default (println "\nThere are" (count deltas)  "local file changes to upload:"))
  sync-state)

(defn- print-complete-message [{:keys [deltas]}]
  (if (not  (empty? deltas)) 
    (println "Sync complete.")))

(defn sync-to-s3 [cred dir-path bucket-name]
  (let [authorised-s3-push (partial push-changes-to-s3 cred)]
    (-> (analyse-sync-state cred dir-path bucket-name)
        (calculate-deltas-from)
        (print-sync-state)
        (authorised-s3-push)
        (print-complete-message))))

