(ns sdkman-java-migrations.adapters.release)

(defn internal->wire
  [{:keys [url]}
   version
   platform]
  {:candidate "java"
   :version   version
   :platform  platform
   :url       url})
