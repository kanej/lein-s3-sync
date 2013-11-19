(ns ^:integration me.kanej.s3-sync.integration-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [aws.sdk.s3 :as s3]
            [me.kanej.s3-sync :as sync]))

(def integration-bucket "s3-sync-integration-test")

(def cred
  (when (.exists (clojure.java.io/file "profiles.clj"))
    (-> (slurp "profiles.clj")
        (read-string)
        (get-in [:dev :s3-sync])
        (select-keys [:access-key :secret-key]))))

(declare setup-tmp-dir)
(declare delete-tmp-dir)
(declare clear-s3-bucket)
(declare check-s3-object)

(deftest an-integration-test
  (setup-tmp-dir)
  (clear-s3-bucket)

  ;; First sync
  (sync/sync-to-s3 cred "./tmp" integration-bucket {:public false})
  (check-s3-object "hello.txt" "5d41402abc4b2a76b9719d911017c592" :private)
  (check-s3-object "world.txt" "7d793037a0760186574b0282f2f435e7" :private)
  (check-s3-object "sub/continent.txt" "d04f9328f41906307def926f7c586933" :private)

  ;; Second sync
  (spit "./tmp/hello.txt" "bonjour" :append true)
  (spit "./tmp/sub/continent.txt" "plate" :append true)
  (sync/sync-to-s3 cred "./tmp" integration-bucket {:public true})
  (check-s3-object "hello.txt" "63434efb5d785a9be8d9b3b95049afcb" :public)
  (check-s3-object "world.txt" "7d793037a0760186574b0282f2f435e7" :private)
  (check-s3-object "sub/continent.txt" "a966e4779266cdbe9a671ab60f08429d" :public)

  ;; Cleanup
  (clear-s3-bucket)
  (delete-tmp-dir))

(defn assert-visibility [visibility grants s3-object-path]
  (let [contains-read-grant (contains? grants {:grantee :all-users :permission :read})]
    (if (= visibility :public)
      (is contains-read-grant (str "The file " s3-object-path " is not public."))
      (is (not contains-read-grant) (str "The file " s3-object-path " is not private.")))))

(defn check-s3-object [s3-object-path expected-md5 visibility]
  (if (s3/object-exists? cred integration-bucket "hello.txt")
    (let [object-response (s3/get-object-metadata cred integration-bucket s3-object-path)
          md5 (get object-response :etag)
          acl-response (s3/get-object-acl cred integration-bucket s3-object-path)
          grants (:grants acl-response)]
      (is (= expected-md5 md5))
      (assert-visibility visibility grants s3-object-path))
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
