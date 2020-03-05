(defproject me.kanej/s3-sync "0.5.3"
  :description "Library for syncing local folders to s3"
  :url "http://github.com/kanej/lein-s3-sync"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :url "http://github.com/kanej/lein-s3-sync"
        :dir ".."}
  :deploy-repositories [["clojars" {:sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-aws-s3 "0.3.6"]
                 [digest "1.4.9"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [lein-light-nrepl "0.0.18"]]}}
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :repl-options {:port 58000
                 :nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]})
