(ns ^:integration leiningen.s3-sync.integration-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [aws.sdk.s3 :as s3]
            [leiningen.s3-sync :as sync]))

(def integration-bucket "s3-sync-integration-test")

(def cred 
  (-> (slurp "profiles.clj")
      (read-string)
      (get-in [:dev :s3-sync])
      (select-keys [:access-key :secret-key])))

(declare setup-tmp-dir)
(declare delete-tmp-dir)
(declare clear-s3-bucket)
(declare check-s3-object)

(deftest an-integration-test
  (setup-tmp-dir)
  (clear-s3-bucket)

  ;; First sync
  (sync/sync-to-s3 cred "./tmp" integration-bucket)
  (check-s3-object "hello.txt" "5d41402abc4b2a76b9719d911017c592")
  (check-s3-object "world.txt" "7d793037a0760186574b0282f2f435e7")
  (check-s3-object "sub/continent.txt" "d04f9328f41906307def926f7c586933")

  ;; Second sync
  (spit "./tmp/hello.txt" "bonjour" :append true) 
  (spit "./tmp/sub/continent.txt" "plate" :append true) 
  (sync/sync-to-s3 cred "./tmp" integration-bucket)
  (check-s3-object "hello.txt" "63434efb5d785a9be8d9b3b95049afcb")
  (check-s3-object "world.txt" "7d793037a0760186574b0282f2f435e7")
  (check-s3-object "sub/continent.txt" "a966e4779266cdbe9a671ab60f08429d")

  ;; Cleanup
  (clear-s3-bucket)
  (delete-tmp-dir))

(defn check-s3-object [s3-object-path expected-md5]
  (if (s3/object-exists? cred integration-bucket "hello.txt")
    (let [response (s3/get-object-metadata cred integration-bucket s3-object-path)
          md5 (get response :etag)]
      (is (= expected-md5 md5))) 
    (is false (str "The file " s3-object-path " did not exist.")))) 

(defn setup-tmp-dir []
    (.mkdir (io/file "./tmp"))
    (.mkdir (io/file "./tmp/sub"))
    (spit "./tmp/hello.txt" "hello")
    (spit "./tmp/world.txt" "world")
    (spit "./tmp/sub/continent.txt" "tectonic")) 

(defn delete-tmp-dir []
  (.delete (io/file "./tmp/hello.txt"))
  (.delete (io/file "./tmp/world.txt"))
  (.delete (io/file "./tmp/sub/continent.txt"))
  (.delete (io/file "./tmp/sub"))
  (.delete (io/file "./tmp")))
 
(defn clear-s3-bucket []
 (s3/delete-object cred integration-bucket "hello.txt")
 (s3/delete-object cred integration-bucket "world.txt")
 (s3/delete-object cred integration-bucket "sub/continent.txt"))

