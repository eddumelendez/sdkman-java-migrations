(ns sdkman-java-migrations.sap-machine
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(def ^:private suffix "-sapmchn")

(def ^:private base-url
  "https://sap.github.io/SapMachine/assets/data/sapmachine_releases.json")

(defn wire->internal
  [{:keys [tag jdk]}
   os
   arch]
  (let [os-arch (keyword (str os "-" arch))]
    {:version (re-find (re-matcher #"\d.*" tag))
     :url     (-> jdk os-arch)}))

(defn fetch-jdk
  [version os arch]
  (let [url (format base-url)
        version' (keyword version)
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (->> (json/read-str body :key-fn keyword)
           :assets
           version'
           :releases
           (map #(wire->internal % os arch))))))

(defn parse-version
  [{:keys [version]}]
  (str version suffix))

(defn main
  [version os arch]
  (let [os'      (if (= os "osx") "mac" os)
        platform (sdkman/platform os' arch)]
    (println (->> (fetch-jdk version os arch)
                  (map #(sdkman/internal->wire % (parse-version %) platform))))))

(defn -main
  [& args]
  (main "11" "linux" "aarch64")
  (main "11" "linux" "x64")
  (main "11" "osx" "x64")
  (main "11" "windows" "x64")

  (main "15" "linux" "aarch64")
  (main "15" "linux" "x64")
  (main "15" "osx" "x64")
  (main "15" "windows" "x64"))
