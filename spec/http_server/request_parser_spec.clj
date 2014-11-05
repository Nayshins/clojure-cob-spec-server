(ns http-server.request-parser-spec
  (:require [http-server.request-parser :refer :all]
            [speclj.core :refer :all]))
(describe "Parse request line"
  (it "returns the action from the request"
    (should= "GET" ((parse-request-line "GET / HTTP/1.1headerContent-Length: 4") :action)))
  (it "returns the location of the request"
    (should= "/" ((parse-request-line "GET / HTTP/1.1headerContent-Length: 4") :location)))
  (it "returns the HTTP version"
    (should= "HTTP/1.1" ((parse-request-line "GET / HTTP/1.1") :http))))

