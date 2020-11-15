(ns sdkman-java-migrations.amazon-corretto
  (:require [clojure.java.shell :as shell]
            [clojure.data.json :as json]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(def ^:private suffix "-amzn")
(def ^:private corretto-url "https://corretto.aws/downloads/resources/%s/amazon-corretto-%s-%s")
(def ^:private base-url "https://api.github.com/repos/corretto/%s/releases")

(defn match-body?
  [pattern
   {:keys [body]}]
  (re-find pattern body))

(defn fetch-jdk
  [repository glob]
  (let [url (format base-url repository)
        {:keys [:exit :err :out]} (shell/sh "curl" url "-H" "accept: application/json" "-H" (str "Authorization: token " (System/getenv "GITHUB_TOKEN")))]
    (if (zero? exit)
      (->> (json/read-str out :key-fn keyword)
           (filter #(match-body? glob %))
           first
           :tag_name)
      (do (println "ERROR:" err)
          (System/exit 1)))))

(defn main
  [repository glob os arch artifact-suffix]
  (let [version (fetch-jdk repository glob)
        platform (sdkman/platform os arch)
        url (format corretto-url version version artifact-suffix)
        sdk-version (str version suffix)]
    (println (sdkman/internal->wire {:url url} sdk-version platform))))

(defn -main
  [& args]
  (main "corretto-8" #"amazon-corretto-[\d.-]+-linux-x64.tar.gz" "linux" "x64" "linux-x64.tar.gz")
  (main "corretto-8" #"amazon-corretto-[\d.-]+-linux-aarch64.tar.gz" "linux" "aarch64" "linux-aarch64.tar.gz")
  (main "corretto-8" #"amazon-corretto-[\d.-]+-windows-x64-jdk.zip" "windows" "x64" "windows-x64-jdk.zip")
  (main "corretto-8" #"amazon-corretto-[\d.-]+-macosx-x64.tar.gz" "mac" "x64" "macosx-x64.tar.gz")

  (main "corretto-11" #"amazon-corretto-[\d.-]+-linux-x64.tar.gz" "linux" "x64" "linux-x64.tar.gz")
  (main "corretto-11" #"amazon-corretto-[\d.-]+-linux-aarch64.tar.gz" "linux" "aarch64" "linux-aarch64.tar.gz")
  (main "corretto-11" #"amazon-corretto-[\d.-]+-windows-x64-jdk.zip" "windows" "x64" "windows-x64-jdk.zip")
  (main "corretto-11" #"amazon-corretto-[\d.-]+-macosx-x64.tar.gz" "mac" "x64" "macosx-x64.tar.gz")

  (main "corretto-jdk" #"amazon-corretto-[\d.-]+-linux-x64.tar.gz" "linux" "x64" "linux-x64.tar.gz")
  (main "corretto-jdk" #"amazon-corretto-[\d.-]+-linux-aarch64.tar.gz" "linux" "aarch64" "linux-aarch64.tar.gz")
  (main "corretto-jdk" #"amazon-corretto-[\d.-]+-windows-x64-jdk.zip" "windows" "x64" "windows-x64-jdk.zip")
  (main "corretto-jdk" #"amazon-corretto-[\d.-]+-macosx-x64.tar.gz" "mac" "x64" "macosx-x64.tar.gz")

  (println "Amazon Corretto Done"))
