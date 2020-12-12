(ns sdkman-java-migrations.adapters.broadcast)

(defn internal->wire
  [version]
  {:candidate "java"
   :version   version
   :url       ""})
