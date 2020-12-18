(ns sdkman-java-migrations.logic.version)

(defn is-valid?
  [version]
  (<= (count version) 17))

(def exist?
  (comp (partial = 302) :status))
