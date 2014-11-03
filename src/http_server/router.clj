(ns http-server.router
  (:require [http_server.response-builder :refer :all]
            [clojure.java.io :as io]
            [base64-clj.core :as base64]
            [pantomime.mime :refer [mime-type-of]])
  (:gen-class))

(def invalid-files `("/file1" "/text-file.txt"))
(def special-routes '("/" "/redirect" "/parameters" "/logs"))

(defn to-byte-array [string]
  (->> string
       (.getBytes)
       (byte-array)))

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
  (let [directory-links (to-byte-array 
                          (build-directory-links directory))]
    (build-response :200 
                    {"Content-Length" (count directory-links)} 
                    directory-links)))


(defn binary-slurp [path]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (io/copy (io/input-stream path) out)
    (.toByteArray out)))

(defn check-auth [auth]
  (let [auth (clojure.string/split auth)]
  (if auth
    (= "admin:hunter2" (base64/decode (second auth))) 
  false)))

(defn get-trimmed-body [body-bytes begin end]
  (->> body-bytes
       (seq)
       (drop begin)
       (take end)
       (byte-array)))

(defn parse-byte-range [byte-header]
  (map #(.replaceAll % "[^0-9]" "") (clojure.string/split byte-header #"-")))

(defn get-file-range [body-bytes range-header path]
  (let [byte-range (parse-byte-range range-header)
        begin (Integer. (first byte-range))
        end (+ 1 (Integer. (second byte-range)))
        body (get-trimmed-body body-bytes begin end)]
    (prn begin end)
    (build-response :206 {"Content-Type"
                         (mime-type-of (io/file path))
                         "Content-Length"
                         (count body)}
                    body)))

(defn get-file-data [directory location headers]
  (try
    (let [path (str directory location)
          body-data (binary-slurp path)]
      (if (contains? headers :Range)
        (get-file-range body-data (headers :Range) path)
      (build-response :200
                      {"Content-Type" 
                       (mime-type-of (io/file path))
                       "Content-Length" 
                       (count body-data) } 
                      body-data)))
    (catch Exception e (build-response :404 {}))))

(defn authenticate [directory location headers]
  (let [no-auth (.getBytes "Authentication required")]
  (if (headers :Authorization)
    (get-file-data directory location headers) 
    (build-response :401 {} no-auth))))

(defn handle-special-route [location directory headers]
  (case location
    "/" (build-directory directory)
    "/logs" (authenticate directory location headers)
    "/redirect" (build-response :301 {"Location" "http://localhost:5000/"})
    (build-response :200 {})))

(defn get-route [location directory headers]
  (if (some (partial = location) special-routes)
    (handle-special-route location directory headers) 
    (get-file-data directory location headers)))

(defn options-route [location directory]
  (build-response :200 {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn not-valid-file [location]
  (some (partial = location) invalid-files))

(defn patch-route [location headers]
  (build-response :200 {}))

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

(defn router [directory parsed-request headers & body]
  (let [action (parsed-request :action)
        location (parsed-request :location)]
    (case action
      "GET" (get-route location directory headers)
      "OPTIONS" (options-route location directory)
      "POST" (post-route (first body) location directory)
      "PATCH" (patch-route headers)
      "PUT" (put-route  (first body) location directory)
      "DELETE" (delete-route location directory)
      (build-response :200 {}))))
