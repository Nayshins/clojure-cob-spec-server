(ns cob-spec-server.startup
  (:require [http-server.cli-options :as cli-options]
            [http-server.server :as server]
            [cob-spec-server.static-router :as static-router]
            [http-server.parameters-helper :as parameter-helper]
            [http-server.directory-helpers :as directory-helper]
            [http-server.handlers :as handlers-helper]))

(def directory "./public")

(defn authenticate [request]
  (let [headers (request :headers)] 
    (if (headers :Authorization)
      (static-router/get-route request directory)
      {:status 401 :body (byte-array (.getBytes "Authentication required"))})))

(defn directory-links [directory]
  (directory-helper/build-directory directory))

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
      {:status 200 :body (parameter-helper/decode-params params)})))

(defn resource-router [request]
  (static-router/router request directory))

(defn not-found [request]
  {:status 404})

(def handlers [app-router parameters-router resource-router not-found])
(defn -main [& args]
  (let [cli-options (cli-options/parse args)]
    (prn "starting server")
    (server/serve 
      (server/create (cli-options :port)) 
      handlers-helper/try-handlers handlers)))
