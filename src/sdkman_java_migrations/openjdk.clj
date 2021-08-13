(ns sdkman-java-migrations.openjdk
  (:require [clj-http.client :as client]
            [clojure.data.xml :as xml]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.zip :as zip]
            [sdkman-java-migrations.controller.version :as controller.version]))

(def ^:private vendor "open")

(def ^:private base-url "https://jdk.java.net/")

(defn ^:private wire->internal
  [url]
  {:version (second (re-find (re-matcher #"openjdk-(\d.[-|\.][^_]+|\d+)" url)))
   :url     url})

(defn ^:private substring
  [body]
  (let [begin-tag "<table class=\"builds\" summary=\"builds\">"
        end-tag "</table>"
        begin-index (str/index-of body begin-tag)
        end-index (+ (str/index-of body end-tag) (count end-tag))]
    (subs body begin-index end-index)))

(defn ^:private fetch-jdk
  [version glob]
  (let [url (str base-url version "/")
        {:keys [status body]} (client/get url)
        href-attr-match? (fn [loc] (re-find glob (zip-xml/attr loc :href)))]
    (when (= 200 status)
      (let [table (-> body substring xml/parse-str zip/xml-zip)
            link (zip-xml/xml1-> table
                                 :tr
                                 :td
                                 :a
                                 href-attr-match?)]
        (wire->internal link)))))

(defn ^:private parse-version
  [version
   jdk]
  (let [jdk-version (:version jdk)
        replace-fn (fn [version pattern] (-> version (str/replace pattern ".ea.") (str/replace #"-\d{1,3}" "")))]
    (cond
      (= version "loom")
      (str (replace-fn jdk-version #"-loom\+") ".lm")

      (= version "panama")
      (str (replace-fn jdk-version #"-panama\+") ".pma")

      :else
      (str/replace jdk-version #"-|\+" "."))))

(defn ^:private main
  [version glob os arch]
  (let [jdk (fetch-jdk version glob)
        sdk-version (parse-version version jdk)]
    (controller.version/migrate! jdk vendor sdk-version os arch)))

(defn -main
  []

  (main 18 #"https.+.openjdk-.+._linux-aarch64_bin.tar.gz" "linux" "aarch64")
  (main 18 #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main 18 #"https.+.openjdk-.+._macos-aarch64_bin.tar.gz" "mac" "aarch64")
  (main 18 #"https.+.openjdk-.+._macos-x64_bin.tar.gz" "mac" "x64")
  (main 18 #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (main 16 #"https.+.openjdk-.+._linux-aarch64_bin.tar.gz" "linux" "aarch64")
  (main 16 #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main 16 #"https.+.openjdk-.+._osx-x64_bin.tar.gz" "mac" "x64")
  (main 16 #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (main "loom" #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main "loom" #"https.+.openjdk-.+._macos-x64_bin.tar.gz" "mac" "x64")
  (main "loom" #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (main "panama" #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main "panama" #"https.+.openjdk-.+._macos-x64_bin.tar.gz" "mac" "x64")
  (main "panama" #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (log/info "OpenJDK Done"))
