<p align="center">
    <a href="https://docs.oracle.com/en/java/javase/23/"><img src="https://img.shields.io/badge/Java-Release%2023-green"/></a>
    <span>&nbsp;</span>
    <a href="https://jitpack.io/#teletha/cointoss"><img src="https://img.shields.io/jitpack/v/github/teletha/cointoss?label=Repository&color=green"></a>
    <span>&nbsp;</span>
    <a href="https://teletha.github.io/cointoss"><img src="https://img.shields.io/website.svg?down_color=red&down_message=CLOSE&label=Official%20Site&up_color=green&up_message=OPEN&url=https%3A%2F%2Fteletha.github.io%2Fcointoss"></a>
</p>







## Prerequisites
Cointoss runs on all major operating systems and requires only [Java version 23](https://docs.oracle.com/en/java/javase/23/) or later to run.
To check, please run `java -version` on your terminal.
<p align="right"><a href="#top">back to top</a></p>

## Install
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
    <groupId>com.github.teletha</groupId>
    <artifactId>cointoss</artifactId>
    <version>1.9.0</version>
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
    implementation 'com.github.teletha:cointoss:1.9.0'
}
```
#### [SBT](https://www.scala-sbt.org/)
Add JitPack repository at the end of resolvers in your build.sbt:
```scala
resolvers += "jitpack" at "https://jitpack.io"
```
Add it into the libraryDependencies section like so:
```scala
libraryDependencies += "com.github.teletha" % "cointoss" % "1.9.0"
```
#### [Leiningen](https://leiningen.org/)
Add JitPack repository at the end of repositories in your project.clj:
```clj
:repositories [["jitpack" "https://jitpack.io"]]
```
Add it into the dependencies section like so:
```clj
:dependencies [[com.github.teletha/cointoss "1.9.0"]]
```
#### [Bee](https://teletha.github.io/bee)
Add it into your project definition class like so:
```java
require("com.github.teletha", "cointoss", "1.9.0");
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


## Dependency
Cointoss depends on the following products on runtime.
* [BorderlessSceneFX-1.0.0](https://mvnrepository.com/artifact/com.catwithawand/BorderlessSceneFX/1.0.0)
* [altfx-1.3.0](https://mvnrepository.com/artifact/com.github.teletha/altfx/1.3.0)
* [asm-9.7](https://mvnrepository.com/artifact/org.ow2.asm/asm/9.7)
* [commons-lang3-3.17.0](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.17.0)
* [commons-net-3.11.1](https://mvnrepository.com/artifact/commons-net/commons-net/3.11.1)
* [conjure-1.1.1](https://mvnrepository.com/artifact/com.github.teletha/conjure/1.1.1)
* [controlsfx-11.2.1](https://mvnrepository.com/artifact/org.controlsfx/controlsfx/11.2.1)
* [failureaccess-1.0.2](https://mvnrepository.com/artifact/com.google.guava/failureaccess/1.0.2)
* [guava-33.3.1-jre](https://mvnrepository.com/artifact/com.google.guava/guava/33.3.1-jre)
* [hypatia-1.1.0](https://mvnrepository.com/artifact/com.github.teletha/hypatia/1.1.0)
* [ikonli-core-12.3.1](https://mvnrepository.com/artifact/org.kordamp.ikonli/ikonli-core/12.3.1)
* [ikonli-fontawesome5-pack-12.3.1](https://mvnrepository.com/artifact/org.kordamp.ikonli/ikonli-fontawesome5-pack/12.3.1)
* [ikonli-javafx-12.3.1](https://mvnrepository.com/artifact/org.kordamp.ikonli/ikonli-javafx/12.3.1)
* [javafx-base-24-ea+15](https://mvnrepository.com/artifact/org.openjfx/javafx-base/24-ea+15)
* [javafx-base-24-ea+15-linux](https://mvnrepository.com/artifact/org.openjfx/javafx-base/24-ea+15)
* [javafx-controls-24-ea+15](https://mvnrepository.com/artifact/org.openjfx/javafx-controls/24-ea+15)
* [javafx-controls-24-ea+15-linux](https://mvnrepository.com/artifact/org.openjfx/javafx-controls/24-ea+15)
* [javafx-graphics-24-ea+15](https://mvnrepository.com/artifact/org.openjfx/javafx-graphics/24-ea+15)
* [javafx-graphics-24-ea+15-linux](https://mvnrepository.com/artifact/org.openjfx/javafx-graphics/24-ea+15)
* [javafx-media-24-ea+15](https://mvnrepository.com/artifact/org.openjfx/javafx-media/24-ea+15)
* [javafx-media-24-ea+15-linux](https://mvnrepository.com/artifact/org.openjfx/javafx-media/24-ea+15)
* [javafx-web-24-ea+15](https://mvnrepository.com/artifact/org.openjfx/javafx-web/24-ea+15)
* [javafx-web-24-ea+15-linux](https://mvnrepository.com/artifact/org.openjfx/javafx-web/24-ea+15)
* [jna-5.12.1](https://mvnrepository.com/artifact/net.java.dev.jna/jna/5.12.1)
* [jna-platform-5.12.1](https://mvnrepository.com/artifact/net.java.dev.jna/jna-platform/5.12.1)
* [lycoris-1.0.0](https://mvnrepository.com/artifact/com.github.teletha/lycoris/1.0.0)
* [openjfx-monocle-jdk-12.0.1+2](https://mvnrepository.com/artifact/org.testfx/openjfx-monocle/jdk-12.0.1+2)
* [primavera-1.1.0](https://mvnrepository.com/artifact/com.github.teletha/primavera/1.1.0)
* [psychopath-2.0.1](https://mvnrepository.com/artifact/com.github.teletha/psychopath/2.0.1)
* [reincarnation-1.8.0](https://mvnrepository.com/artifact/com.github.teletha/reincarnation/1.8.0)
* [sinobu-4.3.0](https://mvnrepository.com/artifact/com.github.teletha/sinobu/4.3.0)
* [sqlite-jdbc-3.47.0.0](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc/3.47.0.0)
* [stylist-1.10.0](https://mvnrepository.com/artifact/com.github.teletha/stylist/1.10.0)
* [typewriter-1.13.0](https://mvnrepository.com/artifact/com.github.teletha/typewriter/1.13.0)
* [univocity-parsers-2.9.1](https://mvnrepository.com/artifact/com.univocity/univocity-parsers/2.9.1)
* [viewtify-2.30.0](https://mvnrepository.com/artifact/com.github.teletha/viewtify/2.30.0)
* [zstd-jni-1.5.6-7](https://mvnrepository.com/artifact/com.github.luben/zstd-jni/1.5.6-7)
<p align="right"><a href="#top">back to top</a></p>


## License
Copyright (C) 2024 The COINTOSS Development Team

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