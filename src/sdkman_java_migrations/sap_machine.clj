(ns sdkman-java-migrations.sap-machine
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "sapmchn")

(def ^:private base-url
  "https://sap.github.io/SapMachine/assets/data/sapmachine_releases.json")

(defn ^:private wire->internal
  [{:keys [tag jdk]}
   os
   arch]
  (let [os-arch (keyword (str os "-" arch))
        version (re-find (re-matcher #"\d.*" tag))
        url (-> jdk os-arch)]
    (when url
      {:version version
       :url     url})))

(defn ^:private fetch-jdk
  [version os arch]
  (let [version' (keyword version)
        {:keys [status body]} (client/get base-url)]
    (when (= 200 status)
      (some-> (json/read-str body :key-fn keyword)
          :assets
          version'
          :releases
          first
          (wire->internal os arch)))))

(defn ^:private main
  [version os arch]
  (let [os' (if (= os "osx") "mac" os)
        jdk (fetch-jdk version os arch)]
    (some-> jdk
            (controller.version/migrate! vendor (:version jdk) os' arch))))

(defn -main
  []
  (main "11" "linux" "aarch64")
  (main "11" "linux" "x64")
  (main "11" "osx" "x64")
  (main "11" "windows" "x64")

  (main "16" "linux" "aarch64")
  (main "16" "linux" "x64")
  (main "16" "osx" "x64")
  (main "16" "windows" "x64")

  (log/info "SapMachine Done"))
