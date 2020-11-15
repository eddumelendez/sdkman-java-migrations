(ns sdkman-java-migrations.util.sdkman)

(def ^:private platforms
  {:linux   {:x64 "LINUX_64"
             :aarch64 "LINUX_ARM64"}
   :mac     {:x64 "MAC_OSX"}
   :windows {:x64 "WINDOWS_64"}})

(defn platform
  [os arch]
  (let [os'   (keyword os)
        arch' (keyword arch)]
    (-> platforms os' arch')))

(defn internal->wire
  [{:keys [url]}
   version
   platform]
  {:candidate "java"
   :version   version
   :platform  platform
   :url       url})
