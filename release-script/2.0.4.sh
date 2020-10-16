./gradlew clean build

./gradlew :hmkit-utils:publish -Prepo=gradle-dev-local -PdepLocation=1
#./gradlew :hmkit-utils:bintrayUpload

./gradlew :hmkit-auto-api:publish -Prepo=gradle-dev-local -PdepLocation=1
#./gradlew :hmkit-auto-bintrayUpload:publish

./gradlew :hmkit-core-jni:publish -Prepo=gradle-dev-local -PdepLocation=1
#./gradlew :hmkit-core-bintrayUpload:publish

./gradlew :hmkit-crypto:publish -Prepo=gradle-dev-local -PdepLocation=1
#./gradlew :hmkit-crypto:bintrayUpload

./gradlew :hmkit-android:publish -Prepo=gradle-dev-local -PdepLocation=1
#./gradlew :hmkit-android:bintrayUpload

./gradlew :command-queue:publish -Prepo=gradle-dev-local -PdepLocation=1
#./gradlew :command-queue:bintrayUpload