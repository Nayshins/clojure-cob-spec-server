(defproject cob-spec-server "0.1.0-SNAPSHOT"
  :description "A simple http server written in clojure"
  :url "www.jakenations.me"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [me.jakenations/http-server "0.2.3-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[speclj "3.1.0"]]}}
  :plugins [[speclj "3.1.0"]]
  :test-paths ["spec"]
  :main cob-spec-server.startup)
