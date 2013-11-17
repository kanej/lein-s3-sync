(require '[leiningen.s3-sync.file-system :as fs]
         '[leiningen.s3-sync.s3 :as s3]
         '[leiningen.s3-sync :as s3s])

(def cred (-> (slurp "profiles.clj")
              (read-string)
              (get-in [:dev :s3-sync])))

(def sync-state (s3s/analyse-sync-state cred "test/example" "s3-sync-integration-test"))

sync-state
