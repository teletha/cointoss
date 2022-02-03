<p align="center">
    <a href="https://docs.oracle.com/en/java/javase/17/"><img src="https://img.shields.io/badge/Java-Release%2017-green"/></a>
    <span>&nbsp;</span>
    <a href="https://jitpack.io/#teletha/cointoss"><img src="https://img.shields.io/jitpack/v/github/teletha/cointoss?label=Repository&color=green"></a>
    <span>&nbsp;</span>
    <a href="https://teletha.github.io/cointoss"><img src="https://img.shields.io/website.svg?down_color=red&down_message=CLOSE&label=Official%20Site&up_color=green&up_message=OPEN&url=https%3A%2F%2Fteletha.github.io%2Fcointoss"></a>
</p>


## About The Project

<p align="right"><a href="#top">back to top</a></p>


## Prerequisites
Cointoss runs on all major operating systems and requires only [Java version 17](https://docs.oracle.com/en/java/javase/17/) or later to run.
To check, please run `java -version` from the command line interface. You should see something like this:
```
> java -version
openjdk version "16" 2021-03-16
OpenJDK Runtime Environment (build 16+36-2231)
OpenJDK 64-Bit Server VM (build 16+36-2231, mixed mode, sharing)
```
<p align="right"><a href="#top">back to top</a></p>

## Using in your build
For any code snippet below, please substitute the version given with the version of Cointoss you wish to use.
#### [Maven](https://maven.apache.org/)
Add JitPack repository at the end of repositories element in your build.xml:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
Add it into in the dependencies element like so:
```xml
<dependency>
    <groupId>cointoss</groupId>
    <artifactId>cointoss</artifactId>
    <version>1.1.0</version>
</dependency>
```
#### [Gradle](https://gradle.org/)
Add JitPack repository at the end of repositories in your build.gradle:
```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```
Add it into the dependencies section like so:
```gradle
dependencies {
    implementation 'cointoss:cointoss:1.1.0'
}
```
#### [SBT](https://www.scala-sbt.org/)
Add JitPack repository at the end of resolvers in your build.sbt:
```scala
resolvers += "jitpack" at "https://jitpack.io"
```
Add it into the libraryDependencies section like so:
```scala
libraryDependencies += "cointoss" % "cointoss" % "1.1.0"
```
#### [Leiningen](https://leiningen.org/)
Add JitPack repository at the end of repositories in your project.clj:
```clj
:repositories [["jitpack" "https://jitpack.io"]]
```
Add it into the dependencies section like so:
```clj
:dependencies [[cointoss/cointoss "1.1.0"]]
```
#### [Bee](https://teletha.github.io/bee)
Add it into your project definition class like so:
```java
require("cointoss", "cointoss", "1.1.0");
```
<p align="right"><a href="#top">back to top</a></p>


## Contributing
Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.
If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

The overwhelming majority of changes to this project don't add new features at all. Optimizations, tests, documentation, refactorings -- these are all part of making this product meet the highest standards of code quality and usability.
Contributing improvements in these areas is much easier, and much less of a hassle, than contributing code for new features.

### Bug Reports
If you come across a bug, please file a bug report. Warning us of a bug is possibly the most valuable contribution you can make to Cointoss.
If you encounter a bug that hasn't already been filed, [please file a report](https://github.com/teletha/cointoss/issues/new) with an [SSCCE](http://sscce.org/) demonstrating the bug.
If you think something might be a bug, but you're not sure, ask on StackOverflow or on [cointoss-discuss](https://github.com/teletha/cointoss/discussions).
<p align="right"><a href="#top">back to top</a></p>


## Built with
Cointoss depends on the following products on runtime.
* [altfx-1.1.1](https://mvnrepository.com/artifact/com.github.teletha/altfx/1.1.1)
* [artoolkitplus-2.3.1-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/artoolkitplus/2.3.1-1.5.5)
* [byteunits-0.9.1](https://mvnrepository.com/artifact/com.jakewharton.byteunits/byteunits/0.9.1)
* [common-image-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.common/common-image/3.1.1)
* [common-io-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.common/common-io/3.1.1)
* [common-lang-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.common/common-lang/3.1.1)
* [commons-codec-1.10](https://mvnrepository.com/artifact/commons-codec/commons-codec/1.10)
* [commons-collections4-4.1](https://mvnrepository.com/artifact/org.apache.commons/commons-collections4/4.1)
* [commons-compress-1.18](https://mvnrepository.com/artifact/org.apache.commons/commons-compress/1.18)
* [commons-io-2.5](https://mvnrepository.com/artifact/commons-io/commons-io/2.5)
* [commons-lang3-3.12.0](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.12.0)
* [commons-math3-3.5](https://mvnrepository.com/artifact/org.apache.commons/commons-math3/3.5)
* [commons-net-3.7.2](https://mvnrepository.com/artifact/commons-net/commons-net/3.7.2)
* [controlsfx-11.1.1](https://mvnrepository.com/artifact/org.controlsfx/controlsfx/11.1.1)
* [datavec-api-1.0.0-M1.1](https://mvnrepository.com/artifact/org.datavec/datavec-api/1.0.0-M1.1)
* [datavec-data-image-1.0.0-M1.1](https://mvnrepository.com/artifact/org.datavec/datavec-data-image/1.0.0-M1.1)
* [deeplearning4j-common-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-common/1.0.0-M1.1)
* [deeplearning4j-core-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-core/1.0.0-M1.1)
* [deeplearning4j-datasets-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-datasets/1.0.0-M1.1)
* [deeplearning4j-datavec-iterators-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-datavec-iterators/1.0.0-M1.1)
* [deeplearning4j-modelimport-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-modelimport/1.0.0-M1.1)
* [deeplearning4j-nn-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-nn/1.0.0-M1.1)
* [deeplearning4j-ui-components-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-ui-components/1.0.0-M1.1)
* [deeplearning4j-utility-iterators-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/deeplearning4j-utility-iterators/1.0.0-M1.1)
* [failureaccess-1.0.1](https://mvnrepository.com/artifact/com.google.guava/failureaccess/1.0.1)
* [fastutil-6.5.7](https://mvnrepository.com/artifact/it.unimi.dsi/fastutil/6.5.7)
* [ffmpeg-4.3.2-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-android-arm](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-android-arm64](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-android-x86](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-android-x86_64](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-linux-arm64](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-linux-armhf](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-linux-ppc64le](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-4.3.2-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg/4.3.2-1.5.5)
* [ffmpeg-platform-4.3.2-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/ffmpeg-platform/4.3.2-1.5.5)
* [flandmark-1.07-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/flandmark/1.07-1.5.5)
* [flatbuffers-java-1.10.0](https://mvnrepository.com/artifact/com.google.flatbuffers/flatbuffers-java/1.10.0)
* [flycapture-2.13.3.31-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/flycapture/2.13.3.31-1.5.5)
* [freemarker-2.3.23](https://mvnrepository.com/artifact/org.freemarker/freemarker/2.3.23)
* [gson-2.8.0](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.0)
* [guava-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/guava/1.0.0-M1.1)
* [guava-31.0.1-jre](https://mvnrepository.com/artifact/com.google.guava/guava/31.0.1-jre)
* [hdf5-1.12.0-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-linux-arm64](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-linux-armhf](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-linux-ppc64le](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-1.12.0-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/hdf5/1.12.0-1.5.5)
* [hdf5-platform-1.12.0-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/hdf5-platform/1.12.0-1.5.5)
* [imageio-bmp-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-bmp/3.1.1)
* [imageio-core-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-core/3.1.1)
* [imageio-jpeg-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-jpeg/3.1.1)
* [imageio-metadata-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-metadata/3.1.1)
* [imageio-psd-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-psd/3.1.1)
* [imageio-tiff-3.1.1](https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-tiff/3.1.1)
* [jackson-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/jackson/1.0.0-M1.1)
* [jackson-annotations-2.12.2](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.12.2)
* [jackson-core-2.12.2](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.12.2)
* [jackson-databind-2.12.2](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.12.2)
* [jai-imageio-core-1.3.0](https://mvnrepository.com/artifact/com.github.jai-imageio/jai-imageio-core/1.3.0)
* [javacpp-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-android-arm](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-android-arm64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-android-x86](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-android-x86_64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-ios-arm64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-ios-x86_64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-linux-arm64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-linux-armhf](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-linux-ppc64le](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-macosx-arm64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/javacpp/1.5.5)
* [javacpp-platform-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/javacpp-platform/1.5.5)
* [javacv-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/javacv/1.5.5)
* [javafx-base-18-ea+10](https://mvnrepository.com/artifact/org.openjfx/javafx-base/18-ea+10)
* [javafx-base-18-ea+10-win](https://mvnrepository.com/artifact/org.openjfx/javafx-base/18-ea+10)
* [javafx-controls-18-ea+10](https://mvnrepository.com/artifact/org.openjfx/javafx-controls/18-ea+10)
* [javafx-controls-18-ea+10-win](https://mvnrepository.com/artifact/org.openjfx/javafx-controls/18-ea+10)
* [javafx-graphics-18-ea+10](https://mvnrepository.com/artifact/org.openjfx/javafx-graphics/18-ea+10)
* [javafx-graphics-18-ea+10-win](https://mvnrepository.com/artifact/org.openjfx/javafx-graphics/18-ea+10)
* [javafx-media-18-ea+10](https://mvnrepository.com/artifact/org.openjfx/javafx-media/18-ea+10)
* [javafx-media-18-ea+10-win](https://mvnrepository.com/artifact/org.openjfx/javafx-media/18-ea+10)
* [javafx-web-18-ea+10](https://mvnrepository.com/artifact/org.openjfx/javafx-web/18-ea+10)
* [javafx-web-18-ea+10-win](https://mvnrepository.com/artifact/org.openjfx/javafx-web/18-ea+10)
* [javax.json-1.0.4](https://mvnrepository.com/artifact/org.glassfish/javax.json/1.0.4)
* [jna-4.3.0](https://mvnrepository.com/artifact/net.java.dev.jna/jna/4.3.0)
* [jna-platform-4.3.0](https://mvnrepository.com/artifact/net.java.dev.jna/jna-platform/4.3.0)
* [joda-time-2.2](https://mvnrepository.com/artifact/joda-time/joda-time/2.2)
* [leptonica-1.80.0-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-android-arm](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-android-arm64](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-android-x86](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-android-x86_64](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-linux-arm64](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-linux-armhf](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-linux-ppc64le](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-1.80.0-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/leptonica/1.80.0-1.5.5)
* [leptonica-platform-1.80.0-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/leptonica-platform/1.80.0-1.5.5)
* [libdc1394-2.2.6-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/libdc1394/2.2.6-1.5.5)
* [libfreenect-0.5.7-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/libfreenect/0.5.7-1.5.5)
* [libfreenect2-0.2.0-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/libfreenect2/0.2.0-1.5.5)
* [librealsense-1.12.4-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/librealsense/1.12.4-1.5.5)
* [librealsense2-2.40.0-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/librealsense2/2.40.0-1.5.5)
* [mkl-2021.1-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/mkl/2021.1-1.5.5)
* [mkl-2021.1-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/mkl/2021.1-1.5.5)
* [mkl-2021.1-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/mkl/2021.1-1.5.5)
* [mkl-2021.1-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/mkl/2021.1-1.5.5)
* [mkl-2021.1-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/mkl/2021.1-1.5.5)
* [mkl-2021.1-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/mkl/2021.1-1.5.5)
* [mkl-platform-2021.1-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/mkl-platform/2021.1-1.5.5)
* [nd4j-api-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/nd4j-api/1.0.0-M1.1)
* [nd4j-common-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/nd4j-common/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-android-arm](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-android-arm64](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-android-x86](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-android-x86_64](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-linux-armhf](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-linux-x86_64](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-macosx-x86_64](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-1.0.0-M1.1-windows-x86_64](https://mvnrepository.com/artifact/org.nd4j/nd4j-native/1.0.0-M1.1)
* [nd4j-native-api-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/nd4j-native-api/1.0.0-M1.1)
* [nd4j-native-platform-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/nd4j-native-platform/1.0.0-M1.1)
* [nd4j-native-preset-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/nd4j-native-preset/1.0.0-M1.1)
* [nd4j-native-preset-1.0.0-M1.1-windows-x86_64](https://mvnrepository.com/artifact/org.nd4j/nd4j-native-preset/1.0.0-M1.1)
* [neoitertools-1.0.0](https://mvnrepository.com/artifact/net.ericaro/neoitertools/1.0.0)
* [openblas-0.3.13-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-android-arm](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-android-arm64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-android-x86](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-android-x86_64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-ios-arm64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-ios-x86_64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-linux-arm64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-linux-armhf](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-linux-ppc64le](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-0.3.13-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/openblas/0.3.13-1.5.5)
* [openblas-platform-0.3.13-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/openblas-platform/0.3.13-1.5.5)
* [opencsv-2.3](https://mvnrepository.com/artifact/net.sf.opencsv/opencsv/2.3)
* [opencv-4.5.1-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-android-arm](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-android-arm64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-android-x86](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-android-x86_64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-ios-arm64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-ios-x86_64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-linux-arm64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-linux-armhf](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-linux-ppc64le](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-linux-x86](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-linux-x86_64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-macosx-x86_64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-windows-x86](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-4.5.1-1.5.5-windows-x86_64](https://mvnrepository.com/artifact/org.bytedeco/opencv/4.5.1-1.5.5)
* [opencv-platform-4.5.1-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/opencv-platform/4.5.1-1.5.5)
* [openjfx-monocle-jdk-12.0.1+2](https://mvnrepository.com/artifact/org.testfx/openjfx-monocle/jdk-12.0.1+2)
* [oshi-core-3.4.2](https://mvnrepository.com/artifact/com.github.oshi/oshi-core/3.4.2)
* [oshi-json-3.4.2](https://mvnrepository.com/artifact/com.github.oshi/oshi-json/3.4.2)
* [oswego-concurrent-1.3.4](https://mvnrepository.com/artifact/org.lucee/oswego-concurrent/1.3.4)
* [protobuf-1.0.0-M1.1](https://mvnrepository.com/artifact/org.nd4j/protobuf/1.0.0-M1.1)
* [psychopath-1.6.0](https://mvnrepository.com/artifact/com.github.teletha/psychopath/1.6.0)
* [rl4j-api-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/rl4j-api/1.0.0-M1.1)
* [rl4j-core-1.0.0-M1.1](https://mvnrepository.com/artifact/org.deeplearning4j/rl4j-core/1.0.0-M1.1)
* [sinobu-2.13.0](https://mvnrepository.com/artifact/com.github.teletha/sinobu/2.13.0)
* [slf4j-api-1.7.21](https://mvnrepository.com/artifact/org.slf4j/slf4j-api/1.7.21)
* [stream-2.9.8](https://mvnrepository.com/artifact/com.clearspring.analytics/stream/2.9.8)
* [stylist-1.4.0](https://mvnrepository.com/artifact/com.github.teletha/stylist/1.4.0)
* [t-digest-3.2](https://mvnrepository.com/artifact/com.tdunning/t-digest/3.2)
* [tesseract-4.1.1-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/tesseract/4.1.1-1.5.5)
* [threetenbp-1.3.3](https://mvnrepository.com/artifact/org.threeten/threetenbp/1.3.3)
* [univocity-parsers-2.9.1](https://mvnrepository.com/artifact/com.univocity/univocity-parsers/2.9.1)
* [videoinput-0.200-1.5.5](https://mvnrepository.com/artifact/org.bytedeco/videoinput/0.200-1.5.5)
* [viewtify-2.2.1](https://mvnrepository.com/artifact/com.github.teletha/viewtify/2.2.1)
* [zstd-jni-1.5.2-1](https://mvnrepository.com/artifact/com.github.luben/zstd-jni/1.5.2-1)

Cointoss depends on the following products on test.
* [HttpClientMock-1.0.0](https://mvnrepository.com/artifact/com.pgs-soft/HttpClientMock/1.0.0)
* [antibug-1.2.7](https://mvnrepository.com/artifact/com.github.teletha/antibug/1.2.7)
* [apiguardian-api-1.1.2](https://mvnrepository.com/artifact/org.apiguardian/apiguardian-api/1.1.2)
* [byte-buddy-1.12.7](https://mvnrepository.com/artifact/net.bytebuddy/byte-buddy/1.12.7)
* [byte-buddy-agent-1.12.7](https://mvnrepository.com/artifact/net.bytebuddy/byte-buddy-agent/1.12.7)
* [decimal4j-1.0.3](https://mvnrepository.com/artifact/org.decimal4j/decimal4j/1.0.3)
* [hamcrest-all-1.3](https://mvnrepository.com/artifact/org.hamcrest/hamcrest-all/1.3)
* [junit-jupiter-api-5.8.2](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api/5.8.2)
* [junit-jupiter-engine-5.8.2](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine/5.8.2)
* [junit-jupiter-params-5.8.2](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-params/5.8.2)
* [junit-platform-commons-1.8.2](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-commons/1.8.2)
* [junit-platform-engine-1.8.2](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-engine/1.8.2)
* [junit-platform-launcher-1.8.2](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-launcher/1.8.2)
* [opentest4j-1.2.0](https://mvnrepository.com/artifact/org.opentest4j/opentest4j/1.2.0)
<p align="right"><a href="#top">back to top</a></p>


## License
Copyright (C) 2022 The COINTOSS Development Team

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
<p align="right"><a href="#top">back to top</a></p>