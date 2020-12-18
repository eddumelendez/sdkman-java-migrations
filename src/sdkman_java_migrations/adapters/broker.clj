(ns sdkman-java-migrations.adapters.broker)

(def ^:private platforms
  {:linux   {:x64     "linux64"
             :x86     "linux64"
             :aarch64 "linuxarm64"
             :arm     "LINUX_ARM64"}
   :mac     {:x64 "darwin"
             :x86 "darwin"}
   :windows {:x64 "msys"
             :x86 "msys"}})

(defn platform
  [os arch]
  (let [os' (keyword os)
        arch' (keyword arch)]
    (-> platforms os' arch')))
