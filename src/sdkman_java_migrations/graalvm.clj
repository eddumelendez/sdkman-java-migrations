(ns sdkman-java-migrations.graalvm
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "grl")
(def ^:private suffix (str "-" vendor))
(def ^:private url "https://api.github.com/repos/graalvm/graalvm-ce-builds/releases")

(defn match-name?
  [pattern
   {:keys [name]}]
  (re-find pattern name))

(defn wire->internal
  [tag url]
  (let [version (re-find (re-matcher #"\d.*" tag))]
    {:version version
     :url     url}))

(defn fetch-jdk
  [glob graal-version]
  (let [{:keys [status body]} (client/get url {:headers {"Authorization" (str "token " (System/getenv "GITHUB_TOKEN"))}})]
    (when (= 200 status)
      (let [release  (->> (json/read-str body :key-fn keyword)
                          (filter #(str/includes? (:tag_name %) (str graal-version)))
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
  (if (= 8 java-version)
    (str version ".r8" suffix)
    (str version ".r11" suffix)))

(defn main
  [version graal-version glob os arch]
  (let [jdk (fetch-jdk glob graal-version)
        sdk-version (parse-version version jdk)]
    (controller.version/migrate! jdk vendor sdk-version os arch)))

(defn -main
  []
  (main 8 21 #"graalvm-ce-java8-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 8 21 #"graalvm-ce-java8-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 8 21 #"graalvm-ce-java8-windows-amd64-.+.zip" "windows" "x64")

  (main 11 21 #"graalvm-ce-java11-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 11 21 #"graalvm-ce-java11-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 11 21 #"graalvm-ce-java11-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 11 21 #"graalvm-ce-java11-windows-amd64-.+.zip" "windows" "x64")

  (main 8 20 #"graalvm-ce-java8-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 8 20 #"graalvm-ce-java8-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 8 20 #"graalvm-ce-java8-windows-amd64-.+.zip" "windows" "x64")

  (main 11 20 #"graalvm-ce-java11-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 11 20 #"graalvm-ce-java11-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 11 20 #"graalvm-ce-java11-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 11 20 #"graalvm-ce-java11-windows-amd64-.+.zip" "windows" "x64")

  (main 8 19 #"graalvm-ce-java8-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 8 19 #"graalvm-ce-java8-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 8 19 #"graalvm-ce-java8-windows-amd64-.+.zip" "windows" "x64")

  (main 11 19 #"graalvm-ce-java11-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 11 19 #"graalvm-ce-java11-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 11 19 #"graalvm-ce-java11-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 11 19 #"graalvm-ce-java11-windows-amd64-.+.zip" "windows" "x64")

  (log/info "GraalVM Done"))
