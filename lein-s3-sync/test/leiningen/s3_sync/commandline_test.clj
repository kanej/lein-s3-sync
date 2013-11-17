(ns leiningen.s3-sync.commandline-test
  (:require [clojure.test :refer :all]
            [leiningen.s3-sync.commandline :as cl]))

(def example-valid-config
  {:access-key "..."
   :secret-key "..."
   :bucket "example-bucket"
   :local-dir "example/dir"})

(deftest valid-when-the-config-includes-all-options
  (let [[result config errors] (cl/resolve-config {:s3-sync example-valid-config} [])]
    (is result)
    (is (= example-valid-config config) "The example config was valid so should be returned.")
    (is (empty? errors))))

(deftest errors-when-no-sync-config-is-defined
  (let [[result config errors] (cl/resolve-config {} [])]
    (is (not result) "No :s3-sync entry should fail validation")
    (is (nil? config) "Should be no config returned when no :s3-sync entry")
    (is (= cl/no-config-msg (first errors)))))

(declare errors-when-missing)

(deftest errors-when-an-option-is-missing-from-config
  (loop [config-keys (keys example-valid-config)]
    (when (not-empty config-keys)
      (errors-when-missing
        (first config-keys)
        {:s3-sync (dissoc example-valid-config (first config-keys))}
        [])
      (recur (rest config-keys)))))

(declare resolve-example-cfg-with)

(deftest config-can-be-overridden-from-command-line
  (are [key args val] (= val (key (resolve-example-cfg-with args)))
       :access-key [":access-key" "updated-value"] "updated-value"
       :secret-key [":secret-key" "updated-value"] "updated-value"
       :bucket     [":bucket" "updated-value"]     "updated-value"
       :local-dir  [":local-dir" "updated-value"]  "updated-value"

       :access-key [":access-key"]                 "..."
       :access-key ["access-key" "ignored"]        "..."
       :access-key ["x" ":access-key" "updated"]   "updated"))

;; Helpers

(defn resolve-example-cfg-with [args]
  (let [[valid config errors] (cl/resolve-config {:s3-sync example-valid-config} args)]
    config))

(defn errors-when-missing [entry project-map command-line-args]
  (let [[result config errors] (cl/resolve-config project-map command-line-args)]
    (is (not result) (str "Should of errored due to missing config entry " entry))
    (is (nil? config) (str "Should be no config returned when missing config entry " entry))
    (is (= [(cl/missing-opt-msg entry)] errors))))

