(ns sdkman-java-migrations.logic.version)

(def exist?
  (comp (partial = 302) :status))
