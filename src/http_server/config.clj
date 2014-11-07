(ns http_server.config
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn combine-into-hashmap [line]
  (let [hash-key (keyword (first line))
        value (second line)
        split-values (map string/trim  (string/split value #","))]
  (hash-map hash-key split-values)))

(defn config-line-parser [config-seq]
  (as-> config-seq __
    (map #(string/split % #": ") __)
    (map combine-into-hashmap __)
    (apply merge __)))

(defn read-config-file [file]
  (with-open [reader (io/reader (io/file file))]
    (config-line-parser (line-seq reader)))) 

