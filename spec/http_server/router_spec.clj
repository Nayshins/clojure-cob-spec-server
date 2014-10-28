(ns http-server.router-spec
  (:require [http-server.router :refer :all]
            [speclj.core :refer :all])
  (:import [java.io File]))

(def path (str (-> (java.io.File. "") .getAbsolutePath) "/public"))

(def test-path 
  (str (-> (java.io.File. "") .getAbsolutePath) "/test"))

(def ok "HTTP/1.1 200 OK\r\n\r\n")

(defn write-to-test [text]
    (spit (str path "/test") text))

(describe "Router"
  (after (write-to-test ""))

  (it "returns file contnets from a GET /file request"
    (should= "HTTP/1.1 200 OK\r\nContent-Length:15\r\nContent-Type:text/plain\r\n\r\nfile1 contents"
             (router path {:action "GET" :location "/file1"})))

  (it "returns directory links from GET /"
    (should= "HTTP/1.1 200 OK\r\nContent-Length:115\r\n\r\n<!DOCTYPE html><html><head><title>directory</title></head><body><a href=\"/file.txt\">file.txt</a><br></body></html>" 
             (router test-path {:action "GET" :location "/"})))
  
  (it "returns 404 when trying to read nonexistent file"
    (should= "HTTP/1.1 404 NOT FOUND\r\n\r\n" 
             (router path {:action "GET" :location "/foobar"})))

  (it "returns allow header with GET POST OPTIONS PUT HEAD from options"
    (should=
      "HTTP/1.1 200 OK\r\nAllow:GET,HEAD,POST,OPTIONS,PUT\r\n\r\n"
      (router path  {:action "OPTIONS" :location "/"})))

  (it "appends body of the request to the requested file POST"
    (write-to-test "test")
    (should= ok 
             (router path {:action "POST" :location "/test"} "test"))
    (should= "testtest" (slurp (str path "/test"))))
  
  (it "PUT overwrites current file content"
    (write-to-test "FAIL")
    (should= ok 
             (router path {:action "PUT" :location "/test"} "PUT test"))
    (should= "PUT test" (slurp (str path "/test"))))
  (it "should not put to protected file"
    (should= "HTTP/1.1 405 METHOD NOT ALLOWED\r\n\r\n"
             (router path {:action "PUT" :location "/file1"} "file1 contents")))

  (it "deletes file contents with DELETE"
    (write-to-test "FAIL")
    (should= ok 
             (router path {:action "DELETE" :location "/test"}))
    (should= "" (slurp (str path "/test")))))
