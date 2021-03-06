(ns sdkman-java-migrations.alibaba-dragonwell
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "albba")
(def ^:private suffix (str "-" vendor))
(def ^:private base-url "https://api.github.com/repos/alibaba/%s/releases")

(defn wire->internal
  [tag url]
  (let [version (re-find (re-matcher #"\d.[^_]+" tag))]
    {:version version
     :url     url}))

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

(defn parse-version
  [{:keys [version]}]
  (str version suffix))

(defn main
  [repository glob os arch]
  (let [jdk (fetch-jdk repository glob)
        sdk-version (parse-version jdk)]
    (controller.version/migrate! jdk vendor sdk-version os arch)))

(defn -main
  []
  (main "dragonwell8" #"Alibaba_Dragonwell_.+._GA_Linux_aarch64.tar.gz" "linux" "aarch64")
  (main "dragonwell8" #"Alibaba_Dragonwell_.+._GA_Linux_x64.tar.gz" "linux" "x64")

  (main "dragonwell11" #"OpenJDK11U-jdk_aarch64_linux_dragonwell_dragonwell-.+.tar.gz" "linux" "aarch64")
  (main "dragonwell11" #"OpenJDK11U-jdk_x64_linux_dragonwell_dragonwell-.+.tar.gz" "linux" "x64")
  (main "dragonwell11" #"OpenJDK11U-jdk_x64_windows_dragonwell_dragonwell-.+.zip" "windows" "x64")

  (log/info "Alibaba Dragonwell Done"))
