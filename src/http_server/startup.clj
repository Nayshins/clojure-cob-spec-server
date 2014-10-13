(ns http-server.startup
 (:require [clojure.tools.cli :refer [parse-opts]]
           [http-server.server :refer :all])
 (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port Number"
    :id :port
    :default 5000
    :parse-fn #(Integer/parseInt %)]
   
   ["-d" "--directory DIRECTORY" "Directory of public folder"
    :id :directory
    :default "~/Public"]])

(defn -main [& args]
  (let [{:keys [options arguments summary]} (parse-opts args cli-options)]
   (server (create-server-socket (options :port)))))
