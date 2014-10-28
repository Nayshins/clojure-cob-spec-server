(ns http-server.router
  (:require [http_server.response-builder :refer :all]
            [clojure.java.io :as io]
            [pantomime.mime :refer [mime-type-of]])
  (:gen-class))

(def invalid-files `("/file1" "/text-file.txt"))

(defn build-directory-links [directory]
  (let [directory (io/file directory)
        files (.list directory)]
    (str "<!DOCTYPE html>"
         "<html>"
         "<head>"
         "<title>directory</title>"
         "</head>"
         "<body>"
         (apply str 
                (map #(str "<a href=\"/" % "\">" % "</a><br>") files))
         "</body>"
         "</html>")))

(defn build-directory [directory]
  (let [directory-links (build-directory-links directory)]
    (build-response :200 
                    {"Content-Length" (count (.getBytes directory-links))} 
                    directory-links)))

(defn get-file-data [directory location]
  (try
  (let [body-data (slurp (str directory location))]
    (build-response :200 
                    {"Content-Type" (mime-type-of (io/file (str directory location)))
                     "Content-Length" (count (.getBytes body-data)) } 
                    body-data))
  (catch Exception e (build-response :404 {}))))

(defn get-route [location directory]
  (if (= location "/")
    (build-directory directory) 
    (get-file-data directory location)))

(defn options-route [location directory]
  (build-response :200 {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn not-valid-file [location]
  (some (partial = location) invalid-files))


(defn post-route [body location directory]
  (cond
    (not-valid-file location) (build-response :405 {})
    :else (do 
            (spit (str directory location) body :append true)
            (build-response :200 {}))))

(defn put-route [body location directory]
  (cond
    (not-valid-file location) (build-response :405 {})
    :else (do 
             (spit (str directory location) body)
             (build-response :200 {}))))

(defn delete-route [location directory]
  (spit (str directory location) "")
  (build-response :200 {}))

(defn router [directory parsed-request & body]
  (let [action (parsed-request :action)
        location (parsed-request :location)]
    (case action
      "GET" (get-route location directory)
      "OPTIONS" (options-route location directory)
      "POST" (post-route (first body) location directory)
      "PUT" (put-route  (first body) location directory)
      "DELETE" (delete-route location directory)
      (build-response :200 {}))))
