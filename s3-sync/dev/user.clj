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
            ;[me.kanej.s3-sync.sync :as s3s]
            ))

(def aws-credentials
  (get-in (read-string (slurp "profiles.clj")) [:dev :s3-sync]))

(def local-file-details (fs/analyse-local-directory "test/example"))
