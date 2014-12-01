(ns http-server.startup-spec
  (:require [speclj.core :refer :all]
            [http-server.startup :refer :all]))

(describe "authenticate"
  
  (it "returns 401 if no authentication"
    (should= 
      401
      (:status 
        (authenticate {:action "GET" :location "/foo" :headers {}}))))
  
  (it "returns 200 if authenticated"
    (should= 200
             (:status 
               (authenticate 
                 {:action "GET" 
                  :location "/logs" 
                  :headers {:Authorization "foo"}})))))

(describe "directory links"
  
  (it "returns a list of the public directory as links"
    (should-contain "<title>directory</title>"
                    (String. (:body (directory-links "./public"))))))

(describe "parameters-router"
  
  (it "returns nil if no parameters"
    (should= nil
             (parameters-router {:action "GET" :location "foo"})))
  
  (it "Returns params as the body of the response"
    (should= "bar"
             (String. (:body 
               (parameters-router {:action "GET" :location "/foo?bar"}))))))
