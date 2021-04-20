(ns sdkman-java-migrations.azul-zulu
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "zulu")

(def ^:private base-url
  (str "https://api.azul.com/zulu/download/community/v1.0/bundles/latest/"
       "?jdk_version=%s"
       "&bundle_type=jdk"
       "&os=%s"
       "&arch=%s"
       "&ext=%s"
       "&hw_bitness=64"
       "&javafx=%s"))

(defn wire->internal
  [{:keys [url jdk_version]}]
  (let [[major minor patch] jdk_version]
    {:version (str major "." minor "." patch)
     :url     url}))

(def ext
  {:linux   "tar.gz"
   :macos   "tar.gz"
   :windows "zip"})

(defn fetch-jdk
  [version os arch fx]
  (let [url (format base-url version os arch ((keyword os) ext) fx)
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (->> (json/read-str body :key-fn keyword)
           (wire->internal)))))

(defn parse-version
  [{:keys [version]}
   fx]
  (if fx
    (str version ".fx")
    version))

(defn main
  ([version os arch]
   (main version os arch false))
  ([version os arch fx]
   (let [os' (if (= os "macos") "mac" os)
         jdk (fetch-jdk version os arch fx)
         sdk-version (parse-version jdk fx)]
     (controller.version/migrate! jdk vendor sdk-version os' arch))))

(defn -main
  []
  (main "7" "linux" "x86")
  (main "7" "macos" "x86")
  (main "7" "windows" "x86")

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
  (main "16" "macos" "x86")
  (main "16" "macos" "arm")
  (main "16" "windows" "x86")

  (main "8" "linux" "x86" true)
  (main "8" "macos" "x86" true)
  (main "8" "windows" "x86" true)

  (main "11" "linux" "x86" true)
  (main "11" "macos" "x86" true)
  (main "11" "windows" "x86" true)

  (main "16" "linux" "x86" true)
  (main "16" "macos" "x86" true)
  (main "16" "windows" "x86" true)

  (log/info "Azul Zulu Done"))
