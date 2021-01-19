./gradlew clean build

./gradlew :hmkit-utils:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-utils:bintrayUpload

./gradlew :hmkit-auto-api:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-auto-api:bintrayUpload

./gradlew :hmkit-core-jni:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-core-jni:bintrayUpload

./gradlew :hmkit-crypto:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-crypto:bintrayUpload

./gradlew :hmkit-android:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :hmkit-android:bintrayUpload

./gradlew :command-queue:publish -Prepo=gradle-release-local -PdepLocation=1
#./gradlew :command-queue:bintrayUpload