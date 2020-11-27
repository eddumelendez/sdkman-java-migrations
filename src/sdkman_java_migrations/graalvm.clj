(ns sdkman-java-migrations.graalvm
  (:require [clojure.java.shell :as shell]
            [clojure.data.json :as json]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(def ^:private suffix "-grl")
(def ^:private url "https://api.github.com/repos/graalvm/graalvm-ce-builds/releases")

(defn match-name?
  [pattern
   {:keys [name]}]
  (re-find pattern name))

(defn match-asset-name?
  [pattern
   {:keys [assets]}]
  (filter #(match-name? pattern %) assets))

(defn wire->internal
  [tag url]
  (let [version (re-find (re-matcher #"\d.*" tag))]
    {:version version
     :url     url}))

(defn fetch-jdk
  [glob]
  (let [{:keys [:exit :err :out]} (shell/sh "curl" url "-H" "accept: application/json" "-H" (str "Authorization: token " (System/getenv "GITHUB_TOKEN")))]
    (if (zero? exit)
      (let [release  (->> (json/read-str out :key-fn keyword)
                          (filter #(match-asset-name? glob %))
                          first)
            tag-name (:tag_name release)
            url      (->> release
                          :assets
                          (filter #(match-name? glob %))
                          first
                          :browser_download_url)]
        (wire->internal tag-name url))
      (do (println "ERROR:" err)
          (System/exit 1)))))

(defn parse-version
  [java-version
   {:keys [version]}]
  (if (= 8 java-version)
    (str version ".r8" suffix)
    (str version ".r11" suffix)))

(defn main
  [version glob os arch]
  (let [jdk (fetch-jdk glob)
        platform (sdkman/platform os arch)
        sdk-version (parse-version version jdk)]
    (println (sdkman/internal->wire jdk sdk-version platform))))

(defn -main
  [& args]
  (main 8 #"graalvm-ce-java8-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 8 #"graalvm-ce-java8-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 8 #"graalvm-ce-java8-windows-amd64-.+.zip" "windows" "x64")

  (main 11 #"graalvm-ce-java11-linux-aarch64-.+.tar.gz" "linux" "aarch64")
  (main 11 #"graalvm-ce-java11-linux-amd64-.+.tar.gz" "linux" "x64")
  (main 11 #"graalvm-ce-java11-darwin-amd64-.+.tar.gz" "mac" "x64")
  (main 11 #"graalvm-ce-java11-windows-amd64-.+.zip" "windows" "x64")

  (println "GraalVM Done"))
