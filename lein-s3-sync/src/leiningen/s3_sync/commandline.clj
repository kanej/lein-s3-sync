(ns leiningen.s3-sync.commandline)

(declare check-for-option)
(declare nil-config-on-error)

(def default-config
  {:access-key nil
   :secret-key nil
   :bucket nil
   :local-dir nil})

(def run-lein-help-for-details
  "\n       Run 'lein help s3-sync' for configuration details.")

(def no-config-msg
  (str
    "Error: No :s3-sync config set in project or profiles."
    run-lein-help-for-details))

(defn missing-opt-msg [option]
  (str "Error: No " option " entry in :s3-sync config"
       run-lein-help-for-details))

(defn lookup-opt [opt-key opts]
    (second (drop-while #(not= % opt-key) opts)))

(defn assoc-from-args [config opt command-line-args]
  (let [val (lookup-opt (str ":" (name opt)) command-line-args)]
    (if-not (nil? val)
      (assoc config opt val)
      config)))

(defn resolve-command-line-config [command-line-args]
  (-> {}
      (assoc-from-args :access-key command-line-args)
      (assoc-from-args :secret-key command-line-args)
      (assoc-from-args :bucket command-line-args)
      (assoc-from-args :local-dir command-line-args)))

(defn- check-that-all-required-opts-are-present [[valid config errors :as result]]
  (if (= config default-config)
    [false nil [no-config-msg]]
    result))

(defn resolve-config [project-map command-line-args]
  (let [command-line-config (resolve-command-line-config command-line-args)
        project-config (get project-map :s3-sync)
        config (merge default-config project-config command-line-config)]
    (-> [true config []]
        (check-that-all-required-opts-are-present)
        (check-for-option :access-key)
        (check-for-option :secret-key)
        (check-for-option :bucket)
        (check-for-option :local-dir)
        (nil-config-on-error))))

(defn- check-for-option [[valid config errors :as result] option]
  (if (and (contains? config option) (not (nil? (option config))))
    result
    [false config (conj errors (missing-opt-msg option))]))

(defn- nil-config-on-error [[valid config errors :as result]]
  (if (not valid)
    [valid nil errors]
    result))

