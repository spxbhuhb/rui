[![Maven Central](https://img.shields.io/maven-central/v/hu.simplexion.rui/rui-runtime)](https://mvnrepository.com/artifact/hu.simplexion.rui/rui-runtime)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![Kotlin](https://img.shields.io/github/languages/top/spxbhuhb/rui)

Rui, short for Reactive UI, is a Kotlin compiler plugin for building reactive user interfaces.

Rui is inspired by [Svelte](https://svelte.io), but it is not a port of Svelte.

Works on JVM and JS at the moment, may work on Native also, depending on the IR support of Native.

## Status

Rui is in **proof-of-concept** status.

## Getting Started

* [Example project](https://github.com/spxbhuhb/rui-example) (should work out-of-the-box)
* [Getting Started](doc/GettingStarted.md)
* [Documentation](doc/README.md)

## Dependencies

Gradle plugin repository (`settings.gradle.kts`, temporary until Gradle registers the plugin):

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
    }
}
```

Gradle plugin dependency (`build.gradle.kts`):

```kotlin
plugin {
    id("hu.simplexion.rui") version "0.1.0"
}
```

Runtime dependency (`build.gradle.kts`):

```kotlin
val commonMain by getting {
    dependencies {
        implementation("hu.simplexion.rui:rui-runtime:0.1.0")
    }
}
```

## Project Structure

| Directory           | Content                                                               |
|---------------------|-----------------------------------------------------------------------|
| `doc`               | Documentation                                                         |
| `rui-gradle-plugin` | Gradle plugin that applies the compiler plugin.                       |
| `rui-kotlin-plugin` | Kotlin compiler plugin that compiles functions into reactive classes. |
| `rui-integraion`    | Test project for plugin development, independently built.             |
| `rui-runtime`       | Library that contains the code required to use the reactive classes.  |

## License

> Copyright (c) 2022-2023 Simplexion Kft, Hungary and contributors
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this work except in compliance with the License.
> You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.