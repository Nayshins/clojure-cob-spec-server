(ns http-server.request-parser
 (:require [clojure.string :as str])
  (:gen-class))

(defn parse-request-line [request]
  (let [request-line (zipmap [:action :location] 
                             (str/split request #" "))]
   request-line))

