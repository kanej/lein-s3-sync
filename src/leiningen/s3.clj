(ns leiningen.s3
  (:require [aws.sdk.s3 :as s3]))

(defn get-file-details-for [cred bucket-name key]
  (let [response (s3/get-object-metadata cred bucket-name key)]
    (assoc response :key key)))

(defn- response->file-details [response]
  {:path (:key response) :md5 (:etag response)} )

(defn analyse-s3-bucket [cred bucket-name file-paths]
  (let [s3-lookup (partial get-file-details-for cred bucket-name)
        bucket-sync-state {:bucket-name bucket-name} ]
    (->> file-paths
         (map s3-lookup)
         (map response->file-details)
         (set)
         (assoc bucket-sync-state :remote-file-details))))

(defn put-file [cred bucket-name key file-path]
  (let [file (clojure.java.io/file file-path)]
    (s3/put-object cred bucket-name key file)))
