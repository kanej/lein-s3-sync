(ns leiningen.s3-sync
  (:require [leiningen.core.main :as lein]
            [leiningen.s3-sync.commandline :as cl]
            [leiningen.s3-sync.file-system :as fs]
            [leiningen.s3-sync.s3 :as s3]
            [leiningen.s3-sync.merge :as m]))

(declare sync-to-s3)

(def padding "                                           ")

(defn s3-sync [project & keys]
  (let [[valid config errors] (cl/resolve-config project keys)]
    (if (not valid)
      (lein/abort (first errors))
      (let [cred (select-keys config [:access-key :secret-key])
            dir-path (:local-dir config)
            bucket-name (:bucket config)]
        (print (str "Syncing bucket " bucket-name " with directory " dir-path))
        (sync-to-s3 cred dir-path bucket-name)
        (flush)))))

(defn analyse-sync-state [cred dir-path bucket-name]
  (let [local-file-state (fs/analyse-local-directory dir-path)
        file-paths (->> local-file-state
                       (:local-file-details)
                       (map :path))
        s3-file-state (s3/analyse-s3-bucket cred bucket-name file-paths)] 
    (merge local-file-state s3-file-state)))

(defn calculate-deltas-from [{:keys [errors] :as sync-state}]
  (if (empty? errors)
    (let [local-file-details (:local-file-details sync-state)
          s3-file-details (:remote-file-details sync-state)
          deltas (m/generate-deltas local-file-details s3-file-details)]
      (assoc sync-state :deltas deltas))
    sync-state))

(defn resolve-full-path [root-path rel-path]
  (let [root (clojure.java.io/file root-path)
        combined (clojure.java.io/file root rel-path)
        abs-path (.getAbsolutePath combined)]
    abs-path))

(defn push-changes-to-s3 [cred {:keys [errors] :as sync-state}]
  (when (empty? errors)
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
            (println "\r  " rel-path "done." padding)
            (recur (rest deltas)))))))
  sync-state)

(defn- print-sync-state [{:keys [errors deltas] :as sync-state}]
  (cond
    (not (empty? errors)) nil
    (empty? deltas) (println "\rThere are no local changes to push." padding)
    (= 1 (count deltas)) (println "\nThere is 1 local file change to upload:")
    :default (println "\nThere are" (count deltas)  "local file changes to upload:"))
  sync-state)

(defn- print-complete-message [{:keys [errors deltas]}]
  (cond 
    (not (empty? errors)) (println (str "\r"  (first errors) padding))
    (not (empty? deltas)) (println "Sync complete.")))

(defn sync-to-s3 [cred dir-path bucket-name]
  (let [authorised-s3-push (partial push-changes-to-s3 cred)]
    (-> (analyse-sync-state cred dir-path bucket-name)
        (calculate-deltas-from)
        (print-sync-state)
        (authorised-s3-push)
        (print-complete-message))))

