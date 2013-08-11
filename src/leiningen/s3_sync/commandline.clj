(ns leiningen.s3-sync.commandline)

(declare check-for-option)
(declare nil-config-on-error)

(def run-lein-help-for-details
  "\n       Run 'lein help s3-sync' for configuration details.")

(def no-config-msg 
  (str 
    "Error: No :s3-sync config set in project or profiles." 
    run-lein-help-for-details))

(defn missing-opt-msg [option]
  (str "Error: No " option " entry in :s3-sync config"
       run-lein-help-for-details))

(defn resolve-config [project-map command-line-args]
  (let [config (get project-map :s3-sync)]
    (if (nil? config)
      [false nil [no-config-msg]]
      (-> [true config []]
          (check-for-option :access-key)
          (check-for-option :secret-key)
          (check-for-option :bucket)
          (check-for-option :local-dir)
          (nil-config-on-error)))))

(defn- check-for-option [[valid config errors :as result] option]
  (if (contains? config option)
    result
    [false config (conj errors (missing-opt-msg option))]))

(defn- nil-config-on-error [[valid config errors :as result]]
  (if (not valid)
    [valid nil errors]
    result))
 
