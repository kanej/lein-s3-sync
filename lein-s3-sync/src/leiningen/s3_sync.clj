(ns leiningen.s3-sync
  (:require [leiningen.core.main :as lein]
            [leiningen.s3-sync.commandline :as cl]
            [me.kanej.s3-sync :as s3s]))

(defn s3-sync
  "Synchronise a directory with a bucket on Amazon's S3.

   The sync operates recursively within the local
   file directory. Files are compared by MD5 hash with
   their remote equivalent and pushed if it does not
   exist or has been changed locally. The synchronisation
   is controlled by config specified on the command line
   or in the project.clj:

   :s3-sync {:access-key \"XXX\"
             :secret-key \"XXX\"
             :bucket \"my-bucket\"
             :local-dir \"out/public\"}

  The bucket given must exist and be accessible."
  [project & keys]
  (let [[valid config errors] (cl/resolve-config project keys)]
    (if (not valid)
      (lein/abort (first errors))
      (let [cred (select-keys config [:access-key :secret-key])
            dir-path (:local-dir config)
            bucket-name (:bucket config)]
        (print (str "Syncing bucket " bucket-name " with directory " dir-path))
        (s3s/sync-to-s3 cred dir-path bucket-name)
        (flush)))))
