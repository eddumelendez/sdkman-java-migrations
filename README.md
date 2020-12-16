# sdkman-java-migrations

The source code in this repository is used by GitHub actions to find the latest version of a given Java vendor and post it to [SDKMAN!](https://github.com/sdkman/).

* AdoptOpenJDK

The `adoptopenjdk` namespace fetch [AdoptOpenJDK API](https://api.adoptopenjdk.net/swagger-ui/) for new versions for `hotspot` and `openj9`.

* Alibaba Dragonwell

The `alibaba-dragonwell` namespace fetch [Alibaba repositories](https://github.com/alibaba/) for new versions.

* Amazon Corretto

The `amazon-correto` namespace fetch [Amazon Corretto repositories](https://github.com/corretto/) for new versions.

* Azul Zulu

The `azul-zulu` namespace fetch [Azul Zulu API](https://app.swaggerhub.com/apis-docs/azul/zulu-download-community/1.0) for new versions.

* Bellsoft Liberica

The `bellsoft-liberica` namespace fetch [Bellsoft Liberica API](https://api.bell-sw.com/api.html) for new versions.

* GraalVM

The `graalvm` namespace fetch [GraalVM Repository](https://github.com/graalvm/graalvm-ce-builds) for new versions.

* OpenJDK

The `openjdk` namespace fetch [jdk.java.net](https://jdk.java.net/) for new versions.

* SapMachine

The `sap-machine` namespace fetch [SapMachine](https://sap.github.io/SapMachine/assets/data/sapmachine_releases.json) for new versions.
