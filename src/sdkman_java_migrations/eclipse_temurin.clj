(ns sdkman-java-migrations.eclipse-temurin
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version])
  (:import [java.net URLEncoder]))

(def ^:private vendor "tem")

(def ^:private base-url
  (str "https://api.adoptium.net/v3/assets/version/%s"
       "?architecture=%s"
       "&heap_size=normal"
       "&image_type=jdk"
       "&jvm_impl=hotspot"
       "&os=%s"
       "&project=jdk"
       "&release_type=ga"
       "&vendor=adoptium"))

(defn wire->internal
  [{:keys [binaries version_data] :as binary}]
  (when binary
    {:version (->> version_data :semver (re-matcher #"(\d.[^-ea+]+)") re-find first)
     :url     (-> binaries first :package :link)}))

(defn fetch-jdk
  [version arch os]
  (let [url (format base-url (URLEncoder/encode version "UTF-8") arch os)
        {:keys [status body]} (client/get url {:throw-exceptions false})]
    (when (= 200 status)
      (->> (json/read-str body :key-fn keyword)
           first
           (wire->internal)))))

(defn main
  [version os arch]
  (let [jdk (fetch-jdk version arch os)]
    (some-> jdk
            (controller.version/migrate! vendor (:version jdk) os arch))))

(defn -main
  []
  (main "[8,9)" "linux" "aarch64")
  (main "[8,9)" "linux" "x64")
  (main "[8,9)" "mac" "x64")
  (main "[8,9)" "windows" "x64")

  (main "[11,12)" "linux" "aarch64")
  (main "[11,12)" "linux" "x64")
  (main "[11,12)" "mac" "x64")
  (main "[11,12)" "windows" "x64")

  (main "[16,17)" "linux" "aarch64")
  (main "[16,17)" "linux" "x64")
  (main "[16,17)"  "mac" "x64")
  (main "[16,17)" "windows" "x64")

  (log/info "Eclipse Temurin Done"))
