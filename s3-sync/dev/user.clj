(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [me.kanej.s3-sync.file-system :as fs]
            [me.kanej.s3-sync.s3 :as s3]
            [me.kanej.s3-sync.merge :as m]
            [aws.sdk.s3 :as aws]
            [me.kanej.s3-sync :as s3s]
            ))

(def cred
  (when (.exists (clojure.java.io/file "profiles.clj"))
    (-> (slurp "profiles.clj")
        (read-string)
        (get-in [:dev :s3-sync])
        (select-keys [:access-key :secret-key]))))

(def bucket-name "s3-sync-test")
(def local-dir "test/example")

(def options (merge cred {:local-dir local-dir :bucket-name bucket-name}))

;;(def sync-state (s3s/analyse-sync-state options))


;;(s3/analyse-s3-bucket cred bucket-name ["world.txt"])


;;(s3/make-file-public aws-credentials "s3-sync-test" "world.txt")
;;(aws/update-object-acl aws-credentials "s3-sync-test" "world.txt" (aws/grant :all-users :read))
