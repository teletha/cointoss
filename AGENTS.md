## Develop

- 受け答えは日本語で行う
- ブラウザで開いて確認はしない

## Build

- ビルドツールは `bee`。`mvn` 単体では `src/main/auto`（`icymanipulator` の APT が生成するソース）がソースルートに含まれずコンパイルできない。
- `mvn clean` は実行しない。事前ビルド済みの `target/classes` を消すと `mvn` では復旧できない。消してしまった場合は `bee compile` で再生成する。
- `src/main/auto` 配下は生成物なので手で編集・コピーしない（ビルドの副作用で差分が出たら `git checkout` で戻す）。

## Test

- `bee compile` で `target/classes` を用意してから、`mvn -o "-Dmaven.repo.local=D:\Application\Java\lib\bee\repository" "-Dtest=<TestClass>" test` でテストする。
- `bee` は Temurin 25、`mvn` は既定で JDK 24 のためクラスバージョンが不一致になる。Maven 実行時は `JAVA_HOME` を bee と同じ JDK（例: `C:\Users\minam\.bee\jdk\temurin-25`）にする。
- `bee test` は依存解決の都合でオフラインでは動かないことがあるので、テストは `mvn` を使う。
