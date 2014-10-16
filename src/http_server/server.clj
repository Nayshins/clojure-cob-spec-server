(ns http-server.server
  (:import [java.io BufferedReader InputStreamReader OutputStreamWriter]
           [java.net ServerSocket Socket SocketException]
           [java.lang Integer])
  (:require [clojure.java.io :refer [reader writer]]
            [http-server.input-parser :refer :all])
  (:gen-class))


(def connection-count (atom 0N))

(defn accept-connection [^ServerSocket server]
  (try
    (.accept server)
    (catch SocketException e)))

(defn create-server-socket [port]
  (ServerSocket. port))

(defn socket-reader [socket]
  (let [reader (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
    reader))

(defn read-request [socket]
  (let [in (socket-reader socket)
        request (.readLine in)]
    request))

(defn write-response [socket response]
  (binding [*out* (OutputStreamWriter. (.getOutputStream socket))]
    (println "HTTP/1.1 200 OK\r\n" )))

(defn socket-handler [socket]
  (with-open [socket socket]
    (let [response (parse-request-headers (read-request socket))]
    (write-response socket response))
    ;parse request -> determine action needed -> return response
    ;let [action (parse-input in)] ?
    ;return response 
    ))

(defn server [server-socket]
    (loop []
      (let [connection (accept-connection server-socket)]
        (future
          (with-open [socket connection]
            (swap! connection-count inc)
            (socket-handler connection) )))
      (if (.isClosed server-socket)
        (reset! connection-count 0N)
        (recur))))



