(ns me.kanej.s3-sync
  (:require [me.kanej.s3-sync.file-system :as fs]
            [me.kanej.s3-sync.s3 :as s3]
            [me.kanej.s3-sync.merge :as m]))

(declare capture-file-details)
(declare calculate-deltas)
(declare push-deltas-to-s3)

(declare print-delta-summary)
(declare print-sync-complete-message)


(defn sync-to-s3
  "Syncronise the local directory 'dir-path' to the S3 bucket 'bucket-name'."
  [{:keys [access-key secret-key] :as cred} dir-path bucket-name]
  (let [absolute-dir-path (fs/path->absolute-path dir-path)
        sync-state {:access-key access-key
                    :secret-key secret-key
                    :local-dir absolute-dir-path
                    :bucket-name bucket-name}]
    (-> sync-state
        (capture-file-details)
        (calculate-deltas)
        (print-delta-summary)
        (push-deltas-to-s3)
        (print-sync-complete-message))))

;; Private functions

(def padding (apply str (take 30 (repeat " "))))

(defn- capture-file-details
  "Pull the local directories file details and the S3 buckets file details
   and associate them with the sync-state."
  [{:keys [access-key secret-key local-dir bucket-name] :as sync-state}]
  (let [cred (select-keys sync-state [:access-key :secret-key])
        local-file-details (fs/analyse-local-directory local-dir)
        file-paths (->> local-file-details
                       (:local-file-details)
                       (map :path))
        remote-file-details (s3/analyse-s3-bucket cred bucket-name file-paths)]
    (merge sync-state {:local-file-details local-file-details
                       :remote-file-details remote-file-details})))

(defn- calculate-deltas
  "Based on the local file details and the remote file details, calculate
   which local files need to be pushed and which do not."
  [{:keys [errors local-file-details remote-file-details] :as sync-state}]
  (if (empty? errors)
    (let [deltas (m/generate-deltas local-file-details remote-file-details)]
      (assoc sync-state :deltas deltas))))

(defn- push-deltas-to-s3
  "Pushes the local files named in the delta list to S3."
  [{:keys [errors local-dir bucket-name deltas] :as sync-state}]
  (when (empty? errors)
    (let [cred (select-keys sync-state [:access-key :secret-key])]
      (loop [deltas deltas]
        (if (not (empty? deltas))
          (let [[op {rel-path :path}] (first deltas)]
            (print "  " rel-path "uploading ...")
            (s3/put-file
             cred
             bucket-name
             rel-path
             (fs/combine-path local-dir rel-path))
            (println "\r  " rel-path "done." padding)
            (recur (rest deltas)))))))
  sync-state)

;; Print Functions

(defn- print-delta-summary [{:keys [errors deltas] :as sync-state}]
  (cond
    (not (empty? errors)) nil
    (empty? deltas) (println "\rThere are no local changes to push." padding)
    (= 1 (count deltas)) (println "\nThere is 1 local file change to upload:")
    :default (println "\nThere are" (count deltas)  "local file changes to upload:"))
  sync-state)

(defn- print-sync-complete-message [{:keys [errors deltas]}]
  (cond
    (not (empty? errors)) (println (str "\r"  (first errors) padding))
    (not (empty? deltas)) (println "Sync complete.")))
