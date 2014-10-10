(ns http-server.core-spec
  (:require [speclj.core :refer :all]
            [http-server.server :refer :all]))
(import '[java.net Socket])

(defn connect []
   (let [socket (Socket. "localhost"  5000)]
            (Thread/sleep 100)
            (.close socket)))

(describe "Server Accepts a Connection"
  (it "creates a connection"
    (let [ss (create-server-socket 5000)]
      (server ss)
      (connect))
    (should= 1 @connection-count)))
