(ns sdkman-java-migrations.mandrel
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "mandrel")
(def ^:private url "https://api.github.com/repos/graalvm/mandrel/releases")

(defn match-name?
  [pattern
   {:keys [name]}]
  (re-find pattern name))

(defn wire->internal
  [tag url]
  (when url
    {:version (re-find (re-matcher #"\d[^-]*" tag))
     :url     url}))

(defn fetch-jdk
  [glob mandrel-version]
  (let [{:keys [status body]} (client/get url {:headers {"Authorization" (str "token " (System/getenv "GITHUB_TOKEN"))}})]
    (when (= 200 status)
      (let [release  (->> (json/read-str body :key-fn keyword)
                          (filter #(str/includes? (:tag_name %) (str mandrel-version)))
                          first)
            tag-name (:tag_name release)
            url      (->> release
                          :assets
                          (filter #(match-name? glob %))
                          first
                          :browser_download_url)]
        (wire->internal tag-name url)))))

(defn parse-version
  [java-version
   {:keys [version]}]
  (cond
    (= 11 java-version)
    (str version ".r11")

    (= 17 java-version)
    (str version ".r17")))

(defn main
  [version mandrel-version glob os arch]
  (let [jdk (fetch-jdk glob mandrel-version)
        sdk-version (parse-version version jdk)]
    (some-> jdk
            (controller.version/migrate! vendor sdk-version os arch))))

(defn -main
  []
  (main 11 22 #"mandrel-java11-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 11 22 #"mandrel-java11-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 11 22 #"mandrel-java11-windows-amd64-.+.zip" "windows" "x64")

  (main 17 22 #"mandrel-java17-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 17 22 #"mandrel-java17-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 17 22 #"mandrel-java17-windows-amd64-.+.zip" "windows" "x64")

  (main 11 21 #"mandrel-java11-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 11 21 #"mandrel-java11-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 11 21 #"mandrel-java11-windows-amd64-.+.zip" "windows" "x64")

  (main 17 21 #"mandrel-java17-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 17 21 #"mandrel-java17-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 17 21 #"mandrel-java17-windows-amd64-.+.zip" "windows" "x64")

  (log/info "Mandrel Done"))
