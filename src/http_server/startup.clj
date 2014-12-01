(ns http-server.startup
  (:require [http-server.cli-options :as cli-options]
            [http-server.server :as server]
            [http-server.resource-handler :as resource-handler]
            [http_server.handlers :as handlers-helper]))

(def directory "./public")

(defn authenticate [request]
  (let [headers (request :headers)] 
    (if (headers :Authorization)
      (resource-handler/get-route request directory)
      {:status 401 :body (byte-array (.getBytes "Authentication required"))})))

(defn directory-links [directory]
  (resource-handler/build-directory directory))

(def routes [["GET" "/" (directory-links directory)]
             ["PUT" "/file1" {:status 405}]
             ["PUT" "/these" {:status 200}]
             ["POST" "/text-file.txt" {:status 405}]
             ["GET" "/redirect" {:status 301
                                 :headers {"Location" "http://localhost:5000/"}}]
             ["GET" "/logs" authenticate]])

(defn app-router [request]
  (some #(handlers-helper/check-route request %) routes))

(defn parameters-router [request]
  (let [query (clojure.string/split (request :location) #"\?")
        location (first query)
        params (second query)]
    (if (nil? params)
      nil
      {:status 200 :body (resource-handler/decode-params params)})))

(defn resource-router [request]
  (resource-handler/router request directory))

(defn not-found [request]
  {:status 404})

(def handlers [app-router parameters-router resource-router not-found])
(defn -main [& args]
  (let [cli-options (cli-options/parse args)]
    (prn "starting server")
    (server/serve 
      (server/create (cli-options :port)) 
      handlers-helper/try-handlers handlers)))
