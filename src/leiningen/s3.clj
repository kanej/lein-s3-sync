(ns leiningen.s3
  (:require [aws.sdk.s3 :as s3]))

(defn get-file-details-for
  "Get the file details for the file in s3.
   Returns nil if there is no file at the given key."
  [cred bucket-name key]
  (try
    (let [response (s3/get-object-metadata cred bucket-name key)]
      (assoc response :key key))
    (catch com.amazonaws.services.s3.model.AmazonS3Exception e
      (when-not (= 404 (.getStatusCode e))
        (throw e)))))

(defn- response->file-details [response]
  {:path (:key response) :md5 (:etag response)} )

(defn analyse-s3-bucket [cred bucket-name file-paths]
  (let [s3-lookup (partial get-file-details-for cred bucket-name)
        bucket-sync-state {:bucket-name bucket-name} ]
    (->> file-paths
         (map s3-lookup)
         (map response->file-details)
         (remove nil?)
         (set)
         (assoc bucket-sync-state :remote-file-details))))

(defn put-file [cred bucket-name key file-path]
  (let [file (clojure.java.io/file file-path)]
    (s3/put-object cred bucket-name key file)))
