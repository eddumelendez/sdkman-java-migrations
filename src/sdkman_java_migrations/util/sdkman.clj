(ns sdkman-java-migrations.util.sdkman
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(def ^:private vendor-release-url "https://vendors.sdkman.io/release")
(def ^:private broadcast-url "https://vendors.sdkman.io/announce/struct")

(def ^:private credentials
  {"Consumer-Key"   (System/getenv "CONSUMER_KEY")
   "Consumer-Token" (System/getenv "CONSUMER_TOKEN")})

(def ^:private platforms
  {:linux   {:x64 "LINUX_64"
             :x86 "LINUX_64"
             :aarch64 "LINUX_ARM64"
             :arm "LINUX_ARM64"}
   :mac     {:x64 "MAC_OSX"
             :x86 "MAC_OSX"}
   :windows {:x64 "WINDOWS_64"
             :x86 "WINDOWS_64"}})

(defn platform
  [os arch]
  (let [os'   (keyword os)
        arch' (keyword arch)]
    (-> platforms os' arch')))

(defn new-version
  [request]
  (client/post vendor-release-url {:headers      credentials
                                   :accept       :json
                                   :content-type :json
                                   :body         (json/write-str request)}))

(defn broadcast
  [request]
  (client/post broadcast-url {:headers      credentials
                              :accept       :json
                              :content-type :json
                              :body         (json/write-str request)}))
