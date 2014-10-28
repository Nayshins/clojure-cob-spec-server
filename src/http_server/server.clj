(ns http-server.server
  (:import [java.io BufferedReader PrintWriter  InputStreamReader OutputStreamWriter]
           [java.net ServerSocket Socket SocketException]
           [java.lang Integer])
  (:require [clojure.java.io :as io]
            [http-server.request-parser :refer :all]
            [http-server.router :refer :all])
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
  (let [writer (io/writer (.getOutputStream socket))]
    writer))

(defn read-headers [in]
  (take-while
    (partial not= "")
    (line-seq in)))

(defn parse-content-length [content-length]
  (as-> content-length  __ 
    (clojure.string/split __ #" ")
    (second __)
    (read-string __)))

(defn get-content-length [headers]
  (if-let [content-length (re-find #"Content-Length: [0-9]+" headers)]
    (parse-content-length content-length)
    0))

(defn read-body [in content-length]
  (let [body (char-array content-length)]
    (.read in body 0 content-length)
    (apply str body)))

(defn read-request [in]
  (let [headers (doall (read-headers in))
        headers (apply str headers) 
        content-length (get-content-length headers) 
        request {:headers headers}]
    (if (> content-length 0)
      (assoc request :body (read-body in content-length))
      request)))


(defn write-response [out response]
  (.write out response)
  (.flush out))

(defn socket-handler [socket directory]
  (with-open [socket socket]
    (let [in (socket-reader socket)
          out (socket-writer socket)
          rri (read-request in)
          parsed-request (parse-request-line (rri :headers))]
      (write-response out (router directory parsed-request (rri :body))))))

(defn server [server-socket directory]
  (loop []
    (let [connection (accept-connection server-socket)]
      (future
        (with-open [socket connection]
          (swap! connection-count inc)
          (socket-handler connection directory))))
    (if (.isClosed server-socket)
      (reset! connection-count 0N)
      (recur))))



