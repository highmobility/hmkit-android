./gradlew clean build

./gradlew :hmkit-utils:publish -Prepo=gradle-release-local
#./gradlew :hmkit-utils:bintrayUpload

./gradlew :hmkit-auto-api:publish -Prepo=gradle-release-local
#./gradlew :hmkit-auto-api:bintrayUpload

./gradlew :hmkit-core-jni:publish -Prepo=gradle-release-local
#./gradlew :hmkit-core-jni:bintrayUpload

./gradlew :hmkit-crypto:publish -Prepo=gradle-release-local
#./gradlew :hmkit-crypto:bintrayUpload

./gradlew :hmkit-android:publish -Prepo=gradle-release-local
#./gradlew :hmkit-android:bintrayUpload

./gradlew :command-queue:publish -Prepo=gradle-release-local
#./gradlew :command-queue:bintrayUpload