(ns sdkman-java-migrations.adapters.broker)

(def ^:private platforms
  {:linux   {:x64     "linux64"
             :x86     "linux64"
             :aarch64 "linuxarm64"
             :arm     "linuxarm64"}
   :mac     {:x64     "darwin"
             :x86     "darwin"
             :aarch64 "darwinarm64"
             :arm     "darwinarm64"}
   :windows {:x64 "msys"
             :x86 "msys"}})

(defn platform
  [os arch]
  (let [os' (keyword os)
        arch' (keyword arch)]
    (-> platforms os' arch')))
