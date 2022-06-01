(ns sdkman-java-migrations.controller.version
  (:require [clojure.tools.logging :as log]
            [sdkman-java-migrations.adapters.broker :as adapters.broker]
            [sdkman-java-migrations.adapters.release :as adapters.release]
            [sdkman-java-migrations.logic.version :as logic.version]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(defn migrate!
  [jdk vendor version os arch]
  (let [vendor-platform (adapters.release/platform os arch)
        broker-platform (adapters.broker/platform os arch)
        version+vendor (str version "-" vendor)]
    (if-not (logic.version/exist? (sdkman/find-version version+vendor broker-platform))
      (println (-> (adapters.release/internal->wire jdk vendor version vendor-platform)
                   (sdkman/new-version)))
      (log/info (str version+vendor " already exist for platform " broker-platform " in sdkman.")))))
