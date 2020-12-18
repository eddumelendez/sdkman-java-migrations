(ns sdkman-java-migrations.adapters.release)

(defn internal->wire
  [{:keys [url]}
   vendor
   version
   platform]
  {:candidate "java"
   :vendor    vendor
   :version   version
   :platform  platform
   :url       url})
