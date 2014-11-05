(ns http-server.router
  (:require [http_server.response-builder :refer :all]
            [http_server.file-interactor :refer :all]
            [clojure.java.io :as io]
            [base64-clj.core :as base64]
            [pantomime.mime :refer [mime-type-of]]
            [pandect.core :refer :all]))

(set! *warn-on-reflection* true)

(def invalid-files '("/file1" "/text-file.txt"))
(def special-routes '("/" "/redirect" "/parameters" "/logs"))

(defn to-byte-array [string]
  (->> ^String string
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
  (map #(.replaceAll ^String % "[^0-9]" "")  (clojure.string/split byte-header #"-")))

(defn get-body-range [body-bytes range-header path]
  (let [byte-range (parse-byte-range range-header)
        begin (Integer. ^String (first byte-range))
        end (+ 1 (Integer. ^String (second byte-range)))
        body (get-trimmed-body body-bytes begin end)]
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
        (get-body-range body-data (headers :Range) path)
        (build-response :200
                        {"Content-Type" 
                         (mime-type-of (io/file path))
                         "Content-Length" 
                         (count body-data) } 
                        body-data)))
    (catch Exception e
      (build-response :404 {}))))

(defn authenticate [directory location headers]
  (let [no-auth (.getBytes "Authentication required")]
    (if (headers :Authorization)
      (get-file-data directory location headers) 
      (build-response :401 {} no-auth))))

(defn decode-params [params]
  (let [params (clojure.string/replace params #"=" " = ")]
    (->> params
         (java.net.URLDecoder/decode)
         (.getBytes)
         (byte-array))))

(defn handle-query [params]
  (let [decoded-params (decode-params params)]
    (build-response :200 
                    {:Content-Length (count decoded-params)} 
                    decoded-params)))

(defn handle-special-route [location directory headers params]
  (case location
    "/" (build-directory directory)
    "/logs" (authenticate directory location headers)
    "/parameters" (handle-query params)
    "/redirect" (build-response :301 {"Location" "http://localhost:5000/"})
    (build-response :200 {})))

(defn get-route [location directory headers] 
  (let [query (clojure.string/split location #"\?") 
        location (first query)
        params (second query)]
    (if (some (partial = location) special-routes)
      (handle-special-route location directory headers params) 
      (get-file-data directory location headers))))

(defn options-route [location directory]
  (build-response :200 {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn not-valid-file [location]
  (some (partial = location) invalid-files))

(defn patch-route [body location directory headers]
  (let [path (str directory location)
        file-data (slurp path)
        encoded-file-data (sha1 file-data)
        etag (headers :If-Match)]
    (overwrite-file path body)
    (build-response :204 {})))

(defn post-route [body location directory]
  (cond
    (not-valid-file location) (build-response :405 {})
    :else (do 
            (append-to-file (str directory location) body)
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
      "PATCH" (patch-route (first body) location directory headers)
      "PUT" (put-route  (first body) location directory)
      "DELETE" (delete-route location directory)
      (build-response :500 {}))))
