(ns http-server.input-parser
 (:require [clojure.string :as str])
  (:gen-class))

(defn parse-request-headers [request]
 (let [parsed-headers (zipmap [:action :location] (str/split "GET / HTTP/1.1" #" "))]
   parsed-headers))

(defn parse-body [body request-map] 
  (let [parsed-body (assoc request-map :body body)]))
