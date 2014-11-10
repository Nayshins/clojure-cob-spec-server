(ns http-server.config-spec
  (:require [http-server.config :refer :all]
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
               '("hello: world, !", "this: is, a, new, line"))))
  (it "does not add a key that has a nil value"
    (should= {:hello '("world")} 
             (config-line-parser
               '("hello: world", "fail: "))))
  
  (it "returns empty hashmap if config does not exist"
    (should= {} (read-config-file "/foo")))
  
  (it "retusn empty hashmap if gonfig is empty"
    (spit "/tmp/test.txt" "")
    (should= {} (read-config-file "/tmp/test.txt"))))
