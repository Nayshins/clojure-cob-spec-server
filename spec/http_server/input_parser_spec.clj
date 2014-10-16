(ns http-server.input-parser-spec
  (:require [http-server.input-parser :refer :all]
            [speclj.core :refer :all]))
(describe "Parse request"
  (it "returns the action from the request"
    (should= "GET" ((parse-request-headers "GET / HTTP/1.1\r\n") :action)))
  (it "returns the location of the request"
      (should= "/" ((parse-request-headers "GET / HTTP/1.1\r\n") :location))))

