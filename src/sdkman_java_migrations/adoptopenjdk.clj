(ns sdkman-java-migrations.adoptopenjdk
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version])
  (:import [java.net URLEncoder]))

(def ^:private vendor "adpt")
(def ^:private suffix (str "-" vendor))

(def ^:private implementations
  {:hotspot "hs"
   :openj9  "j9"})

(defn ^:private base-url
  [impl]
  (let [url (str "https://api.adoptopenjdk.net/v3/assets/version/%s"
                 "?architecture=%s"
                 "&heap_size=normal"
                 "&image_type=jdk"
                 "&os=%s"
                 "&release_type=ga"
                 "&vendor=%s")]
    (if impl
      (str url "&jvm_impl=%s")
      url)))

(defn wire->internal
  [{:keys [binaries version_data]}]
  {:version (:semver version_data)
   :url     (-> binaries first :package :link)})

(defn fetch-jdk
  [version arch impl os vendor]
  (let [base-url' (base-url impl)
        url (if impl
              (format base-url' (URLEncoder/encode version "UTF-8") arch os vendor impl)
              (format base-url' (URLEncoder/encode version "UTF-8") arch os vendor))
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (->> (json/read-str body :key-fn keyword)
           first
           (wire->internal)))))

(defn parse-version
  [{:keys [version]}
   vendor
   impl]
  (cond
    (= vendor "adoptopenjdk")
    (let [implementation (keyword impl)]
      (str version "." (implementation implementations) suffix))

    (= vendor "openjdk")
    (str version "-open")))

(defn main
  ([version os arch vendor]
   (main version os arch vendor nil))
  ([version os arch vendor impl]
  (let [jdk (fetch-jdk version arch impl os vendor)
        sdk-version (parse-version jdk vendor impl)]
    (controller.version/migrate! jdk vendor sdk-version os arch))))

(defn -main
  []
  (main "[8,9)" "linux" "x64" "adoptopenjdk" "hotspot")
  (main "[8,9)" "linux" "x64" "adoptopenjdk" "openj9")

  (main "[8,9)" "linux" "aarch64" "adoptopenjdk" "hotspot")
  (main "[8,9)" "linux" "aarch64" "adoptopenjdk" "openj9")

  (main "[8,9)" "windows" "x64" "adoptopenjdk" "hotspot")
  (main "[8,9)" "windows" "x64" "adoptopenjdk" "openj9")

  (main "[8,9)" "mac" "x64" "adoptopenjdk" "hotspot")
  (main "[8,9)" "mac" "x64" "adoptopenjdk" "openj9")

  (main "[11,12)" "linux" "x64" "adoptopenjdk" "hotspot")
  (main "[11,12)" "linux" "x64" "adoptopenjdk" "openj9")

  (main "[11,12)" "linux" "aarch64" "adoptopenjdk" "hotspot")
  (main "[11,12)" "linux" "aarch64" "adoptopenjdk" "openj9")

  (main "[11,12)" "windows" "x64" "adoptopenjdk" "hotspot")
  (main "[11,12)" "windows" "x64" "adoptopenjdk" "openj9")

  (main "[11,12)" "mac" "x64" "adoptopenjdk" "hotspot")
  (main "[11,12)" "mac" "x64" "adoptopenjdk" "openj9")

  (main "[15,16)" "linux" "x64" "adoptopenjdk" "hotspot")
  (main "[15,16)" "linux" "x64" "adoptopenjdk" "openj9")

  (main "[15,16)" "linux" "aarch64" "adoptopenjdk" "hotspot")
  (main "[15,16)" "linux" "aarch64" "adoptopenjdk" "openj9")

  (main "[15,16)" "windows" "x64" "adoptopenjdk" "hotspot")
  (main "[15,16)" "windows" "x64" "adoptopenjdk" "openj9")

  (main "[15,16)"  "mac" "x64" "adoptopenjdk" "hotspot")
  (main "[15,16)" "mac" "x64" "adoptopenjdk" "openj9")

  (main "[8,9)" "linux" "x64" "adoptopenjdk" "hotspot")
  (main "[8,9)" "linux" "x64" "adoptopenjdk" "openj9")

  (main "[8,9)" "linux" "x64" "openjdk")
  (main "[8,9)" "windows" "x64" "openjdk")

  (main "[11,12)" "linux" "aarch64" "openjdk")
  (main "[11,12)" "linux" "x64" "openjdk")
  (main "[11,12)" "windows" "x64" "openjdk")

  (log/info "AdoptOpenJDK Done"))
