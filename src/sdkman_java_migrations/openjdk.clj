(ns sdkman-java-migrations.openjdk
  (:require [clj-http.client :as client]
            [clojure.data.xml :as xml]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]
            [sdkman-java-migrations.adapters.release :as adapters.release]
            [sdkman-java-migrations.util.sdkman :as sdkman]
            [clojure.string :as str]))

(def ^:private vendor "open")
(def ^:private suffix (str "-" vendor))

(def base-url "https://jdk.java.net/")

(defn wire->internal
  [url]
  {:version (second (re-find (re-matcher #"[^\w](\d.[-|\.][^_]+)" url)))
   :url     url})

(defn ^:private substring
  [body]
  (let [begin-tag "<table class=\"builds\" summary=\"builds\">"
        end-tag "</table>"
        begin-index (str/index-of body begin-tag)
        end-index (+ (str/index-of body end-tag) (count end-tag))]
    (subs body begin-index end-index)))

(defn attr-pattern
  [attrname pattern]
  (fn [loc]
    (re-find pattern (zip-xml/attr loc attrname))))

(defn fetch-jdk
  [version glob]
  (let [url (str base-url version)
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (let [table (zip/xml-zip (xml/parse-str (substring body)))
            link (zip-xml/xml1-> table
                                 :tr
                                 :td
                                 :a
                                 (attr-pattern :href glob))]
        (wire->internal link)))))

(defn parse-version
  [version
   jdk]
  (let [replace-fn (fn [version pattern] (-> version (str/replace pattern ".ea.") (str/replace #"-\d{1,3}" "")))]
    (cond
      (= version "loom")
      (str (replace-fn (:version jdk) #"-loom\+") ".lm" suffix)

      (= version "panama")
      (str (replace-fn (:version jdk) #"-panama\+") ".pma" suffix)

      :else
      (str (str/replace (:version jdk) #"-|\+" ".") suffix))))

(defn main
  [version glob os arch]
  (let [jdk (fetch-jdk version glob)
        platform (sdkman/platform os arch)
        sdk-version (parse-version version jdk)]
    (println (-> (adapters.release/internal->wire jdk sdk-version platform)))))

(defn -main
  [& args]

  (main 16 #"https.+.openjdk-.+._linux-aarch64_bin.tar.gz" "linux" "aarch64")
  (main 16 #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main 16 #"https.+.openjdk-.+._osx-x64_bin.tar.gz" "mac" "x64")
  (main 16 #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (main "loom" #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main "loom" #"https.+.openjdk-.+._osx-x64_bin.tar.gz" "mac" "x64")
  (main "loom" #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (main "panama" #"https.+.openjdk-.+._linux-x64_bin.tar.gz" "linux" "x64")
  (main "panama" #"https.+.openjdk-.+._osx-x64_bin.tar.gz" "mac" "x64")
  (main "panama" #"https.+.openjdk-.+._windows-x64_bin.zip" "windows" "x64")

  (println "OpenJDK Done"))
