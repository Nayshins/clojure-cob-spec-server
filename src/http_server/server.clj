(ns http-server.server
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))
(import '[java.io BufferedReader InputStreamReader OutputStreamWriter]
        '[java.net ServerSocket Socket SocketException]
        '[java.lang Integer])

(def connection-count (atom 0N))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :id :port
    :default 5000
    :parse-fn #(Integer. %)]

   ["-d" "--directory DIRECTORY"  "Directory"
    :id :directory
    :default "~/Public"]])

(defn handle-connection [^ServerSocket server]
    (try
      (.accept server)
      (catch SocketException e)))

(defn create-server-socket [port]
  (ServerSocket. port))

(defn server [server-socket]
    (future
     (let [connection (handle-connection server-socket)]
      (with-open [socket connection]
        (swap! connection-count inc)))))

(defn -main [& args])

