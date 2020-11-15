(ns sdkman-java-migrations.adoptopenjdk
  (:require [clojure.java.shell :as shell]
            [clojure.data.json :as json]
            [sdkman-java-migrations.util.sdkman :as sdkman])
  (:import [java.net URLEncoder]))

(def ^:private suffix "-adpt")

(def ^:private implementations
  {:hotspot "hs"
   :openj9  "j9"})

(def ^:private base-url
  (str "https://api.adoptopenjdk.net/v3/assets/version/%s"
       "?architecture=%s"
       "&heap_size=normal"
       "&image_type=jdk"
       "&jvm_impl=%s"
       "&os=%s"
       "&release_type=ga"
       "&vendor=adoptopenjdk"))

(defn wire->internal
  [{:keys [binaries version_data]}]
  {:version (:semver version_data)
   :url     (-> binaries first :package :link)})

(defn fetch-jdk
  [version arch impl os]
  (let [url (format base-url (URLEncoder/encode version "UTF-8") arch impl os)
        {:keys [:exit :err :out]} (shell/sh "curl" url "-H" "accept: application/json")]
    (if (zero? exit)
      (->> (json/read-str out :key-fn keyword)
           first
           (wire->internal))
      (do (println "ERROR:" err)
          (System/exit 1)))))

(defn parse-version
  [{:keys [version]}
   impl]
  (let [implementation (keyword impl)]
    (str version "." (implementation implementations) suffix)))

(defn main
  [version impl os arch]
  (let [platform (sdkman/platform os arch)
        last-jdk (fetch-jdk version arch impl os)]
    (println (sdkman/internal->wire last-jdk (parse-version last-jdk impl) platform))))

(main "[8,9)" "hotspot" "linux" "x64")
(main "[8,9)" "openj9" "linux" "x64")

(main "[8,9)" "hotspot" "linux" "aarch64")
(main "[8,9)" "openj9" "linux" "aarch64")

(main "[8,9)" "hotspot" "windows" "x64")
(main "[8,9)" "openj9" "windows" "x64")

(main "[8,9)" "hotspot" "mac" "x64")
(main "[8,9)" "openj9" "mac" "x64")

(main "[11,12)" "hotspot" "linux" "x64")
(main "[11,12)" "openj9" "linux" "x64")

(main "[11,12)" "hotspot" "linux" "aarch64")
(main "[11,12)" "openj9" "linux" "aarch64")

(main "[11,12)" "hotspot" "windows" "x64")
(main "[11,12)" "openj9" "windows" "x64")

(main "[11,12)" "hotspot" "mac" "x64")
(main "[11,12)" "openj9" "mac" "x64")

(main "[15,16)" "hotspot" "linux" "x64")
(main "[15,16)" "openj9" "linux" "x64")

(main "[15,16)" "hotspot" "linux" "aarch64")
(main "[15,16)" "openj9" "linux" "aarch64")

(main "[15,16)" "hotspot" "windows" "x64")
(main "[15,16)" "openj9" "windows" "x64")

(main "[15,16)" "hotspot" "mac" "x64")
(main "[15,16)" "openj9" "mac" "x64")
