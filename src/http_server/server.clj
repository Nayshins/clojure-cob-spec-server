(ns http-server.server
  (:import [java.io BufferedReader InputStreamReader OutputStreamWriter]
           [java.net ServerSocket Socket SocketException]
           [java.lang Integer])
  (:gen-class))


(def connection-count (atom 0N))

(defn handle-connection [^ServerSocket server]
  (try
    (.accept server)
    (catch SocketException e)))

(defn create-server-socket [port]
  (ServerSocket. port))

(defn read-input [socket]
  (binding [*in* (BufferedReader. (InputStreamReader. (.getInputStream socket)))
            *out* (OutputStreamWriter. (.getOutputStream socket))]
    (do 
    (read-line) 
    (println "HTTP/1.1 200 OK")
    (.close socket))))

(defn server [server-socket]
    (loop []
      (let [connection (handle-connection server-socket)]
        (future
          (with-open [socket connection]
            (swap! connection-count inc)
            (read-input connection) )))
      (if (.isClosed server-socket)
        (reset! connection-count 0N) 
        (recur))))



