(ns leiningen.s3-sync.commandline-test
  (:require [clojure.test :refer :all]
            [leiningen.s3-sync.commandline :as cl]))

(def example-valid-config
  {:access-key "..."
   :secret-key "..."
   :bucket "example-bucket"
   :local-dir "example/dir"})

(declare errors-when-missing)

(deftest valid-when-the-config-includes-all-options
  (let [[result config errors] (cl/resolve-config {:s3-sync example-valid-config} [])]
    (is result)
    (is (= example-valid-config config) "The example config was valid so should be returned.")
    (is (empty? errors))))

(deftest errors-when-no-sync-config-is-defined
  (let [[result config errors] (cl/resolve-config {} [])]
    (is (not result) "No :s3-sync entry should fail validation")
    (is (nil? config) "Should be no config returned when no :s3-sync entry")
    (is (= [cl/no-config-msg] errors))))

(deftest errors-when-an-option-is-missing-from-config
  (errors-when-missing :access-key {:s3-sync (dissoc example-valid-config :access-key)} [])
  (errors-when-missing :secret-key {:s3-sync (dissoc example-valid-config :secret-key)} [])
  (errors-when-missing :bucket {:s3-sync (dissoc example-valid-config :bucket)} [])
  (errors-when-missing :local-dir {:s3-sync (dissoc example-valid-config :local-dir)} []))

;; Helpers

(defn errors-when-missing [entry project-map command-line-args]
  (let [[result config errors] (cl/resolve-config project-map command-line-args)]
    (is (not result) (str "Should of errored due to missing config entry " entry))
    (is (nil? config) (str "Should be no config returned when missing config entry " entry))
    (is (= [(cl/missing-opt-msg entry)] errors))))
