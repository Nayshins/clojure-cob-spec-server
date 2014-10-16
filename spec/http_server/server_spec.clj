(ns http-server.server-spec
  (:require [speclj.core :refer :all]
            [http-server.server :refer :all]
            [clojure.java.io :refer [reader writer]])
  (:import [java.net Socket]))

(defn connect []
   (with-open [socket (Socket. "localhost"  5000)]
            (Thread/sleep 100)))

(defn multiple-connect [connections]
  (dotimes [n connections]
    (connect)))

(defn test-input-output [request]
  (with-open [socket (Socket. "localhost" 4000)
              out (writer socket)
              in (reader socket)]
    (Thread/sleep 100)
    (.write out request)
    (.flush out)
    (.readLine in)))

(describe "create-server"
  (it "creates a ServerSocket"
    (with-open [server-socket (create-server-socket 5000)]
      (should-be-a java.net.ServerSocket server-socket))))

(describe "server"
  (it "accepts a connection"
      (with-open [ss (create-server-socket 5000)]
        (future (server ss))
        (connect)
      (should= 1 @connection-count)))
  (it "accepts many connections"
      (with-open [ss (create-server-socket 5000)]
        (future (server ss))
        (multiple-connect 9))
      (should= 10 @connection-count)))

(describe "socket handler"
  (it "returns 200 OK on / request"
      (with-open [ss (create-server-socket 4000)]
        (future (server ss))
        (should= "HTTP/1.1 200 OK" (test-input-output "/\n")))))

