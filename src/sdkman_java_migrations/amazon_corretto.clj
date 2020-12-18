(ns sdkman-java-migrations.amazon-corretto
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "amzn")
(def ^:private suffix (str "-" vendor))
(def ^:private base-url "https://api.github.com/repos/corretto/%s/releases")

(defn ^:private match-body?
  [pattern
   {:keys [body]}]
  (re-find pattern body))

(defn ^:private wire->internal
  [tag url]
  {:version tag
   :url     url})

(defn ^:private fetch-jdk
  [repository glob]
  (let [url (format base-url repository)
        {:keys [status body]} (client/get url {:headers {"Authorization" (str "token " (System/getenv "GITHUB_TOKEN"))}})]
    (when (= 200 status)
      (let [release (->> (json/read-str body :key-fn keyword)
                         (filter #(match-body? glob %))
                         first)
            tag (:tag_name release)
            artifact-url (->> release :body (re-matcher glob) re-find second)]
        (wire->internal tag artifact-url)))))

(defn ^:private main
  [repository glob os arch]
  (let [jdk (fetch-jdk repository glob)
        sdk-version (str (:version jdk) suffix)]
    (controller.version/migrate! jdk vendor sdk-version os arch)))

(defn -main
  []
  (main "corretto-8" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-linux-aarch64.tar.gz)\)" "linux" "aarch64")
  (main "corretto-8" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-linux-x64.tar.gz)\)" "linux" "x64")
  (main "corretto-8" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-windows-x64-jdk.zip)\)" "windows" "x64")
  (main "corretto-8" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-macosx-x64.tar.gz)\)" "mac" "x64")

  (main "corretto-11" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-linux-aarch64.tar.gz)\)" "linux" "aarch64")
  (main "corretto-11" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-linux-x64.tar.gz)\)" "linux" "x64")
  (main "corretto-11" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-windows-x64-jdk.zip)\)" "windows" "x64")
  (main "corretto-11" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-macosx-x64.tar.gz)\)" "mac" "x64")

  (main "corretto-jdk" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-linux-aarch64.tar.gz)\)" "linux" "aarch64")
  (main "corretto-jdk" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-linux-x64.tar.gz)\)" "linux" "x64")
  (main "corretto-jdk" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-windows-x64-jdk.zip)\)" "windows" "x64")
  (main "corretto-jdk" #"\((https:\/\/corretto.aws.+.amazon-corretto-[\d.-]+-macosx-x64.tar.gz)\)" "mac" "x64")

  (log/info "Amazon Corretto Done"))
