(ns leiningen.s3-sync.merge)

(defn generate-deltas [local-file-details s3-file-details]
  (let [upload-file-details (clojure.set/difference local-file-details s3-file-details)]
    (set (map #(vector :upload %) upload-file-details))))
