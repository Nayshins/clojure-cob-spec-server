(ns http-server.config
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn split-values [values]
  (if (empty? values)
    ""
  (map string/trim  (string/split values #","))))


(defn combine-into-hashmap [line]
  (let [hash-key (keyword (first line))
        value (second line)
        values-list (split-values value)]
    (if (empty? values-list)
      {}
      (hash-map hash-key values-list))))

(defn config-line-parser [config-seq]
  (as-> config-seq __
    (remove empty? __)
    (map #(string/split % #": ") __)
    (map combine-into-hashmap __)
    (apply merge __)))

(defn read-config-file [file]
  (with-open [reader (io/reader (io/file file))]
    (config-line-parser (line-seq reader)))) 

