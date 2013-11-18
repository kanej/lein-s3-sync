(ns me.kanej.s3-sync
  (:require [me.kanej.s3-sync.file-system :as fs]
            [me.kanej.s3-sync.s3 :as s3]
            [me.kanej.s3-sync.merge :as m]))

(def padding (apply str (take 30 (repeat " "))))

(defn analyse-sync-state [cred dir-path bucket-name]
  (let [local-file-state (fs/analyse-local-directory dir-path)
        file-paths (->> local-file-state
                       (:local-file-details)
                       (map :path))
        s3-file-state (s3/analyse-s3-bucket cred bucket-name file-paths)]
    (merge local-file-state s3-file-state)))

(defn calculate-deltas-from [{:keys [errors local-file-details remote-file-details] :as sync-state}]
  (if (empty? errors)
    (let [deltas (m/generate-deltas local-file-details remote-file-details)]
      (assoc sync-state :deltas deltas))))

(defn resolve-full-path [root-path rel-path]
  (let [root (clojure.java.io/file root-path)
        combined (clojure.java.io/file root rel-path)
        abs-path (.getAbsolutePath combined)]
    abs-path))

(defn push-changes-to-s3 [cred {:keys [errors root-dir-path bucket-name] :as sync-state}]
  (when (empty? errors)
    (loop [deltas (:deltas sync-state)]
      (if (not (empty? deltas))
        (let [[op {rel-path :path}] (first deltas)]
          (print "  " rel-path "uploading ...")
          (s3/put-file
            cred
            bucket-name
            rel-path
            (resolve-full-path root-dir-path rel-path))
          (println "\r  " rel-path "done." padding)
          (recur (rest deltas))))))
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
