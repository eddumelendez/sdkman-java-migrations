(ns sdkman-java-migrations.bellsoft-liberica
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "librca")

(def ^:private base-url
  (str "https://api.bell-sw.com/v1/liberica/releases"
       "?arch=%s"
       "&bitness=64"
       "&bundle-type=%s"
       "&os=%s"
       "&package-type=%s"
       "&version-feature=%s"
       "&version-modifier=latest"))

(defn ^:private wire->internal
  [{:keys [downloadUrl featureVersion interimVersion updateVersion]}]
  (let [version (str featureVersion "." interimVersion "." updateVersion)]
    {:version version
     :url     downloadUrl}))

(def ^:private package-type
  {:linux   "tar.gz"
   :macos   "zip"
   :windows "zip"})

(defn ^:private fetch-jdk
  [arch os version-feature fx]
  (let [bundle-type (if fx "jdk-full" "jdk")
        url (format base-url arch bundle-type os ((keyword os) package-type) version-feature)
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (some->> (json/read-str body :key-fn keyword)
               first
               (wire->internal)))))

(defn ^:private parse-version
  [{:keys [version]} fx]
  (if fx
    (str version ".fx")
    version))

(defn ^:private main
  ([version-feature os arch]
   (main version-feature os arch false))
  ([version-feature os arch fx]
   (let [os' (if (= os "macos") "mac" os)
         jdk (fetch-jdk arch os version-feature fx)
         sdk-version (parse-version jdk fx)]
     (some-> jdk
             (controller.version/migrate! vendor sdk-version os' arch)))))

(defn -main
  []
  (main "8" "linux" "arm")
  (main "8" "linux" "x86")
  (main "8" "macos" "arm")
  (main "8" "macos" "x86")
  (main "8" "windows" "x86")

  (main "11" "linux" "arm")
  (main "11" "linux" "x86")
  (main "11" "macos" "arm")
  (main "11" "macos" "x86")
  (main "11" "windows" "x86")

  (main "16" "linux" "arm")
  (main "16" "linux" "x86")
  (main "16" "macos" "arm")
  (main "16" "macos" "x86")
  (main "16" "windows" "x86")

  (main "8" "linux" "x86" true)
  (main "8" "macos" "x86" true)
  (main "8" "windows" "x86" true)

  (main "11" "linux" "arm" true)
  (main "11" "linux" "x86" true)
  (main "11" "macos" "x86" true)
  (main "11" "windows" "x86" true)

  (main "16" "linux" "arm" true)
  (main "16" "linux" "x86" true)
  (main "16" "macos" "x86" true)
  (main "16" "windows" "x86" true)

  (log/info "Bellsoft Liberica Done"))
