(ns me.kanej.s3-sync
  (:require [me.kanej.s3-sync.file-system :as fs]
            [me.kanej.s3-sync.s3 :as s3]
            [me.kanej.s3-sync.merge :as m]))

(def padding (apply str (take 30 (repeat " "))))

(defn analyse-sync-state [{:keys [access-key secret-key local-dir bucket-name] :as options}]
  (let [cred (select-keys options [:access-key :secret-key])
        local-file-details (fs/analyse-local-directory local-dir)
        file-paths (->> local-file-details
                       (:local-file-details)
                       (map :path))
        s3-file-state (s3/analyse-s3-bucket cred bucket-name file-paths)]
    (merge
      {:local-file-details local-file-details :root-dir-path local-dir}
      s3-file-state)))

(defn calculate-deltas-from [{:keys [errors local-file-details remote-file-details] :as sync-state}]
  (if (empty? errors)
    (let [deltas (m/generate-deltas local-file-details remote-file-details)]
      (assoc sync-state :deltas deltas))))

(declare resolve-full-path)

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

(declare path->absolute-path)
(declare print-sync-state)
(declare print-complete-message)


(defn sync-to-s3 [{:keys [access-key secret-key] :as cred} dir-path bucket-name]
  (let [authorised-s3-push (partial push-changes-to-s3 cred)
        absolute-dir-path (fs/path->absolute-path dir-path)]
    (-> {:access-key access-key :secret-key secret-key :local-dir absolute-dir-path :bucket-name bucket-name}
        (analyse-sync-state)
        (calculate-deltas-from)
        (print-sync-state)
        (authorised-s3-push)
        (print-complete-message))))

;; Helper functions

(defn resolve-full-path [root-path rel-path]
  (let [root (clojure.java.io/file root-path)
        combined (clojure.java.io/file root rel-path)
        abs-path (.getAbsolutePath combined)]
    abs-path))

;; Print Functions

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
