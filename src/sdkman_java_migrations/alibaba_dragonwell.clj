(ns sdkman-java-migrations.alibaba-dragonwell
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "albba")
(def ^:private base-url "https://api.github.com/repos/alibaba/%s/releases")

(defn wire->internal
  [tag url]
  (when url
    (let [version (re-find (re-matcher #"\d.[^_]+" tag))]
      {:version version
       :url     url})))

(defn fetch-jdk
  [repository glob]
  (let [url (format base-url repository)
        {:keys [status body]} (client/get url {:headers {"Authorization" (str "token " (System/getenv "GITHUB_TOKEN"))}})
        match-name? (fn match-name? [pattern {:keys [name]}] (re-find pattern name))
        match-asset-name? (fn match-asset-name? [pattern {:keys [assets]}] (filter #(match-name? pattern %) assets))]
    (when (= 200 status)
      (let [release (->> (json/read-str body :key-fn keyword)
                         (filter #(match-asset-name? glob %))
                         first)
            tag-name (:tag_name release)
            url (->> release
                     :assets
                     (filter #(match-name? glob %))
                     first
                     :browser_download_url)]
        (wire->internal tag-name url)))))

(defn main
  [repository glob os arch]
  (let [jdk (fetch-jdk repository glob)]
    (some-> jdk
            (controller.version/migrate! vendor (:version jdk) os arch))))

(defn -main
  []
  (main "dragonwell8" #"Alibaba_Dragonwell_.+._aarch64_linux.tar.gz" "linux" "aarch64")
  (main "dragonwell8" #"Alibaba_Dragonwell_.+._x64_linux.tar.gz" "linux" "x64")
  (main "dragonwell8" #"Alibaba_Dragonwell_.+._x64_windows.zip" "windows" "x64")

  (main "dragonwell11" #"Alibaba_Dragonwell_.+._aarch64_linux.tar.gz" "linux" "aarch64")
  (main "dragonwell11" #"Alibaba_Dragonwell_.+._x64_linux.tar.gz" "linux" "x64")
  (main "dragonwell11" #"Alibaba_Dragonwell_.+._x64_windows.zip" "windows" "x64")

  (main "dragonwell17" #"Alibaba_Dragonwell_.+._aarch64_linux.tar.gz" "linux" "aarch64")
  (main "dragonwell17" #"Alibaba_Dragonwell_.+._x64_linux.tar.gz" "linux" "x64")
  (main "dragonwell17" #"Alibaba_Dragonwell_.+._x64_windows.zip" "windows" "x64")

  (log/info "Alibaba Dragonwell Done"))
