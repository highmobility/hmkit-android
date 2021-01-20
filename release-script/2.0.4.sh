./gradlew clean build

./gradlew :hmkit-utils:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-utils:bintrayUpload -PdepLocation=1

./gradlew :hmkit-auto-api:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-auto-api:bintrayUpload -PdepLocation=1

./gradlew :hmkit-core-jni:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-core-jni:bintrayUpload -PdepLocation=1

./gradlew :hmkit-crypto:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-crypto:bintrayUpload -PdepLocation=1

./gradlew :hmkit-android:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-android:bintrayUpload -PdepLocation=1

./gradlew :command-queue:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :command-queue:bintrayUpload -PdepLocation=1