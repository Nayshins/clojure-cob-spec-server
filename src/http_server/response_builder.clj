(ns http_server.response-builder
  (:require [clojure.java.io :as io])
  (:gen-class))

(def response-code { :200 "200 OK\r\n"
                     :404 "404 NOT FOUND\r\n"
                     :405 "405 METHOD NOT ALLOWED\r\n"})

(defn build-code [code]
  (str "HTTP/1.1 " (response-code code)))

(defn build-headers [headers]
  (if (not-empty headers)
    (->> headers
         (map #(str (key %) ":" (val %) "\r\n"))
         (apply str))))

(defn build-body [body]
  (if-not (nil? (first body))
  (first body)))

(defn build-response [code headers & body]
  (str (build-code code)
       (build-headers headers) "\r\n"
       (build-body body)))
