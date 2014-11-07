(ns http_server.config-spec
  (:require [http_server.config :refer :all]
            [speclj.core :refer :all]))

(describe "read-config-file"
  (it "reads a file and parses it"
    (spit "/tmp/test.txt" "hello: world, !\nthis: is, a, test")
    (should= {:hello '("world", "!")
              :this '("is", "a", "test")} 
             (read-config-file "/tmp/test.txt"))))

(describe "config-line-parser"
  (it "creates a hashmap of values split by :"
    (should= {:hello '("world")} (config-line-parser '("hello: world"))))
  
  (it "splits the values on , into a sequence"
    (should= {:hello '("world", "!")} 
             (config-line-parser '("hello: world, !"))))
  
  (it "parses multiple list elements"
    (should= {:hello '("world", "!")
              :this '("is","a","new","line")}
             (config-line-parser 
               '("hello: world, !", "this: is, a, new, line")))))
