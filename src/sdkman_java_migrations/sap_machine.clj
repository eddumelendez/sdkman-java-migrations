(ns sdkman-java-migrations.sap-machine
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.adapters.release :as adapters.release]
            [sdkman-java-migrations.logic.version :as logic.version]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(def ^:private vendor "sapmchn")
(def ^:private suffix (str "-" vendor))

(def ^:private base-url
  "https://sap.github.io/SapMachine/assets/data/sapmachine_releases.json")

(defn ^:private wire->internal
  [{:keys [tag jdk]}
   os
   arch]
  (let [os-arch (keyword (str os "-" arch))
        version (re-find (re-matcher #"\d.*" tag))]
    {:version version
     :url     (-> jdk os-arch)}))

(defn ^:private fetch-jdk
  [version os arch]
  (let [url (format base-url)
        version' (keyword version)
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (-> (json/read-str body :key-fn keyword)
          :assets
          version'
          :releases
          first
          (wire->internal os arch)))))

(defn ^:private parse-version
  [{:keys [version]}]
  (str version suffix))

(defn ^:private main
  [version os arch]
  (let [os' (if (= os "osx") "mac" os)
        platform (sdkman/platform os' arch)
        jdk (fetch-jdk version os arch)
        sdk-version (parse-version jdk)]
    (if (logic.version/is-valid? sdk-version)
      (println (-> (adapters.release/internal->wire jdk vendor sdk-version platform)))
      (log/warn (str sdk-version " exceeds length.")))))

(defn -main
  []
  (main "11" "linux" "aarch64")
  (main "11" "linux" "x64")
  (main "11" "osx" "x64")
  (main "11" "windows" "x64")

  (main "15" "linux" "aarch64")
  (main "15" "linux" "x64")
  (main "15" "osx" "x64")
  (main "15" "windows" "x64"))
