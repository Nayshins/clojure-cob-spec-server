(ns http-server.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))
(import '[java.io BufferedReader InputStreamReader OutputStreamWriter]
        '[java.net ServerSocket Socket SocketException]
        '[java.lang Integer])


(defn echo [socket]
  (binding [*in* (BufferedReader. (InputStreamReader. (.getInputStream socket)))
            *out* (OutputStreamWriter. (.getOutputStream socket))] 
   (loop []
      (println (read-line))
      (recur))))

(defn create-server
  [port fun]
  (fun (.accept (ServerSocket. port))))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :id :port
    :default 5000
    :parse-fn #(Integer. %)]

   ["-d" "--directory DIRECTORY"  "Directory"
    :id :directory
    :default "~/Public"]])

(defn -main [& args]
  (let [{:keys [options args errors summary]} (parse-opts args cli-options)]
    (create-server (options :port) echo)))

