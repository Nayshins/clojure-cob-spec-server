(ns http-server.server
  (:import [java.io BufferedReader PrintWriter  InputStreamReader OutputStreamWriter]
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

(defn socket-writer [socket]
  (let [writer (PrintWriter. (.getOutputStream socket))]
    writer))

(defn read-request [in]
  (let [request (.read in)]
    request))

(defn read-headers [in]
  (take-while 
    (partial not="")
    (line-seq in)))

(defn write-response [out]
  (.println out "HTTP/1.1 200 OK\r\n")
  (.flush out))

(defn socket-handler [socket]
  (with-open [socket socket]
    (let [in (socket-reader socket)
          out (socket-writer socket)]
      (read-request in)
      (write-response out))
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



