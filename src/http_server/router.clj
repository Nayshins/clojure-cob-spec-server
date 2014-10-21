(ns http-server.router
  (:gen-class))

(def invalid-files `("/file1" "/text-file.txt"))

(defn build-response [body-data]
  (str "HTTP/1.1 200 OK\r\n\r\n" (clojure.string/trim-newline body-data)))

(defn get-route [location directory]
  (if (= location "/")
    "HTTP/1.1 200 OK\r\n\r\n"
  (let [body-data (slurp (str directory location))]
    (build-response body-data))))

(defn options-route [location directory]
  "HTTP/1.1 200 OK\r\n Allow: GET,HEAD,POST,OPTIONS,PUT \r\n\r\n\r\n\r\n")

(defn not-valid-file [location]
  (some (partial = location) invalid-files))


(defn post-route [body location directory]
  (cond
    (not-valid-file location) "HTTP/1.1 405 METHOD NOT ALLOWED\r\n\r\n\r\n\r\n"
    :else (do 
            (spit (str directory location) body :append true)
            "HTTP/1.1 200 OK\r\n\r\n\r\n\r\n")))

(defn put-route [body location directory]
  (cond
    (not-valid-file location) "HTTP/1.1 405 METHOD NOT ALLOWED\r\n\r\n\r\n\r\n"
    :else (do 
             (spit (str directory location) body)
             "HTTP/1.1 200 OK\r\n\r\n\r\n\r\n")))

(defn delete-route [location directory]
  (spit (str directory location) "")
  "HTTP/1.1 200 OK\r\n\r\n\r\n\r\n")

(defn router [directory parsed-request & body]
  ; GET get a file -> return file and 200
  ; Post write to a file -> return 200
  (let [action (parsed-request :action)
        location (parsed-request :location)]
    (case action
      "GET" (get-route location directory)
      "OPTIONS" (options-route location directory)
      "POST" (post-route (first body) location directory)
      "PUT" (put-route  (first body) location directory)
      "DELETE" (delete-route location directory)
      "HTTP/1.1 200 OK\r\nheader\r\n\r\n\r\n\r\n")))
