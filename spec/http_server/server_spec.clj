(ns http-server.server-spec
  (:require [speclj.core :refer :all]
            [http-server.server :refer :all]
            [clojure.java.io :refer [reader writer]])
  (:import [java.net Socket]
           [java.io BufferedReader InputStreamReader StringReader]
           [org.apache.commons.io.IOUtils]))


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
    (.write out request)
    (.flush out)
    (Thread/sleep 100)
    (.readLine in)))

(describe "create-server"
  (it "creates a ServerSocket"
    (with-open [server-socket (create-server-socket 5000)]
      (should-be-a java.net.ServerSocket server-socket))))

(describe "server"
  (it "accepts a connection"
    (with-open [ss (create-server-socket 5000)]
      (future (server ss 
                 "/Users/Nayshins/desktop/projects/http-server/public"))
        (connect)
      (should= 1 @connection-count)))

  (it "accepts many connections"
      (with-open [ss (create-server-socket 5000)]
        (future (server ss
                   "/Users/Nayshins/desktop/projects/http-server/public"))
        (multiple-connect 9))
      (should= 10 @connection-count)))

(describe "get content length"
  (it "gets the length from header"
    (should= 4 (get-content-length {:Content-Length 4})))
  
  (it "should return 0 for headers without content length"
    (should= 0 (get-content-length {:Content-Length nil}))))

(describe "convert headers to hashmap"
  (it "converts header lazy seq to hashmap"
    (should= "value"
             (let [string-seq
                   (line-seq (BufferedReader. 
                               (StringReader. "key: value\nx-ray: foxtrot")))]
               ((convert-headers-to-hashmap string-seq) :key)))))

(describe "request reader"
  (it "reads all of the request headers"
     (let [reader (BufferedReader.
                    (InputStreamReader. 
                      (org.apache.commons.io.IOUtils/toInputStream
                        "GET / HTTP/1.1\r\nheader: hello\r\nContent-Length: 4\r\n\r\nbody\r\n\r\n")))
           headers ((read-request reader) :headers)]
     (should= {:header "hello", :Content-Length "4"}
              headers)
     (should-not-contain "body" headers)))

  (it "reads the body of the request"
    (let [reader (BufferedReader.
                  (InputStreamReader.
                    (org.apache.commons.io.IOUtils/toInputStream
                      "GET / HTTP/1.1\r\nContent-Length: 4\r\n\r\nbody\r\n\r\n")))]
      (should= "body" ((read-request reader) :body)))))

(describe "socket handler"
  (it "returns 200 OK on GET / request"
      (with-open [ss (create-server-socket 4000)]
        (future (server ss 
                  "/Users/Nayshins/desktop/projects/http-server/public"))
        (should= "HTTP/1.1 200 OK" (test-input-output 
                                     "GET / HTTP/1.1\r\nContent-Length: 0\r\n\r\n")))))

