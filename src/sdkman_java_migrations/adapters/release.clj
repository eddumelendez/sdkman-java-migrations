(ns sdkman-java-migrations.adapters.release)

(def ^:private platforms
  {:linux   {:x64     "LINUX_64"
             :x86     "LINUX_64"
             :aarch64 "LINUX_ARM64"
             :arm     "LINUX_ARM64"}
   :mac     {:x64     "MAC_OSX"
             :x86     "MAC_OSX"
             :aarch64 "MAC_ARM64"
             :arm     "MAC_ARM64"}
   :windows {:x64 "WINDOWS_64"
             :x86 "WINDOWS_64"}})

(defn platform
  [os arch]
  (let [os'   (keyword os)
        arch' (keyword arch)]
    (-> platforms os' arch')))

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
