(ns http-server.request-parser
 (:require [clojure.string :as str]
           [clojure.tools.logging :as log])
  (:gen-class))

(defn parse-request-line [request]
  (let [request-line (zipmap [:action :location :http] 
                             (str/split request #" "))]
   request-line))

