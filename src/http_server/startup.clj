(ns http-server.startup
  (:require [clojure.tools.cli :refer [parse-opts]]
            [http-server.server :refer :all])
  (:import [java.io File])
  (:gen-class))

(def directory (atom ""))

(def cli-options
  [["-p" "--port PORT" "Port Number"
    :id :port
    :default 5000
    :parse-fn #(Integer/parseInt %)]

   ["-d" "--directory DIRECTORY" "Directory of public folder"
    :id :directory
    :default (str (->  (java.io.File. "") .getAbsolutePath) "/public")]])

(defn -main [& args]
  (let [{:keys [options arguments summary]} (parse-opts args cli-options)]
    (reset! directory (options :directory))
    (let  [form  (clojure.java.io/file @directory "form")]
      (.createNewFile form)
      (.deleteOnExit form))
    (server (create-server-socket (options :port)) (options :directory))))
