(ns sdkman-java-migrations.util.sdkman
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [slingshot.slingshot :refer [try+]]))

(def ^:private vendor-release-url "https://vendors.sdkman.io/release")
(def ^:private broadcast-url "https://vendors.sdkman.io/announce/struct")
(def ^:private broker-url "https://api.sdkman.io/2/broker/download/java/%s/%s")

(def ^:private credentials
  {"Consumer-Key"   (System/getenv "CONSUMER_KEY")
   "Consumer-Token" (System/getenv "CONSUMER_TOKEN")})

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

(defn find-version
  [version+vendor platform]
  (let [url (format broker-url version+vendor platform)]
    (try+
      (client/get url {:redirect-strategy :none})
      (catch [:status 404] {:keys [status]}
        {:status status}))))
