# ResChiper

<h1 align="center">
  <img src="artifacts/reschiper-banner.png" alt="failed to load artifacts/logo.png"/>
  <p align="center" style="font-size: 0.3em">AAB Resource Obfuscation Tool</p>
</h1>

[![License](https://img.shields.io/badge/license-Apache2.0-maroon)](LICENSE)
[![BundleTool](https://img.shields.io/badge/Dependency-Bundletool/1.0.0-red)](https://github.com/google/bundletool)
![JDK Version](https://img.shields.io/badge/JDK-17-blue)
![Gradle Version](https://img.shields.io/badge/Gradle-8.0-darkgreen)
[![ResChiper Version](https://img.shields.io/badge/release-0.1.0--rc-%23C6782A.svg?style=flat)](https://github.com/goldfish07/ResChiper/releases/tag/0.1.0-rc1)

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting-started)
    - [Requirements](#requirements)
    - [Installation](#installation)
    - [Usage](#usage)
- [Configuration Options](#configuration-options)
- [Output](#output)
- [Acknowledgments](#acknowledgments)
- [License](#license)

## Introduction

ResChiper is a tool designed for obfuscating Android AAB resources.
It allows you to protect your resources from unauthorized access and reduce your app's AAB size.

## Getting Started

Follow these steps to integrate the AAB Resource Obfuscation Tool into your Android project:

## Requirements

Before you begin using ResChiper, ensure that your app meets the following requirements:

- **Java Development Kit (JDK)**: ResChiper requires JDK 17, Make sure your app is configured with JDK 17.
- **Android Gradle Plugin (AGP)**: version 8.0 or later version.

## Installation

#### 1. Add ResChiper Gradle Plugin

In your project's root-level `build.gradle` file, add the ResChiper Gradle plugin to the `buildscript` section:

```gradle
buildscript {
  dependencies {
    classpath "io.github.goldfish07.reschiper:plugin:<latest_version>"
  }
  
  repositories {
    mavenCentral()
    google()
   }
}
```

#### 2. Apply the Plugin

In your app-level `build.gradle` file, apply the ResChiper plugin:

```gradle
apply plugin: "io.github.goldfish07.reschiper"
```

#### 3. Configure the Plugin

In your `app/build.gradle` file, configure the ResChiper plugin by specifying your desired settings. Here's an example
configuration:

```gradle
resChiper {
    enableObfuscation = true //by default res obfuscate is enabled
    obfuscationMode = "default" //["dir", "file", "default"]
    obfuscatedBundleName = "reschiper-app.aab" // Obfuscated file name, must end with '.aab'
    //mappingFile = file("path/to/your/mapping.txt").toPath() // Mapping file used for incremental obfuscation
    whiteList = [ // White list rules (resource name to exclude)
                  "*.R.raw.*",
                  "*.R.drawable.ic_launcher"
    ]
    mergeDuplicateResources = true // allow the merge of duplicate resources
    enableFileFiltering = true
    enableFilterStrings = true
    fileFilterList = [ // file filter rules
                       "META-INF/*",
                       "*/armeabi-v7a/*",
                       "*/arm64-v8a/*",
                       "*/x86/*",
                       "*/x86_64/*"
    ]
    unusedStringFile = "path/to/your/unused_strings.txt" // strings will be filtered in this file
    localeWhiteList = ["en", "in", "fr"] //keep en,en-xx,in,in-xx,fr,fr-xx and remove others locale.
}
```

## Usage

To obfuscate your resources and generate an obfuscated AAB, run the following Gradle command in the project's root
directory.:

```cmd
./gradle clean :app:resChiperDebug --stacktrace
```

This command will execute the obfuscation process from the project root, and the obfuscated AAB will be generated in
the `app/build/outputs/bundle/debug` directory.

## Configuration Options

The ResChiper extension provides various configuration options for resource obfuscation, including enabling/disabling
obfuscation, specifying mapping files, white-listing resources, and more.

- `enableObfuscation`: Enable or disable resource obfuscation.<br>
- `obfuscationMode`: to obfuscate only directories set `obfuscationMode = "dir"`, to obfuscate only files set
  `obfuscationMode = "file"` and to obfuscate both directory and files set `obfuscationMode = "default"`.<br>
- `enableFilterStrings`: Input the unused file splits by lines to support remove strings.<br>
- `enableFileFiltering`: Support for filtering files in the bundle package. Currently only supports filtering in
  the `META-INFO/` and `lib/` paths.<br>
- `obfuscatedBundleName`: Name of the obfuscated AAB file.<br>
- `mergeDuplicateResources`: eliminate duplicate resource files and reduce package size.<br>
- `mappingFile`: Path to the ProGuard mapping file (set only when mapping.txt used for obfuscation).<br>
- `whiteList`: Set of resource names to exclude from obfuscation.<br>
- `fileFilterList`: List of file patterns to filter out.<br>
- `unusedStringFile`: Path to a file containing unused strings.<br>
- `localeWhiteList`: Set of locales to include in the AAB.

## Example

you can check some configuration example [here](https://github.com/goldfish07/ResChiper/wiki/Example-Configuration-Options) 

## Output

After running the obfuscation process, you can expect the following output files:

- **aab:** This is the obfuscated bundle package, which contains your Android App Bundle (AAB) with obfuscated
  resources.
- **resources-mapping.txt:** This file contains the resource obfuscation mapping. It can be used as input for future
  obfuscation processes to achieve incremental obfuscation. This is especially useful if you want to maintain
  consistency across different builds.
- **-duplicated.txt:** This log file provides information about merged resources. It helps you identify and track any
  duplicate resources that were merged during the obfuscation process.

These output files will be generated as a result of running the ResChiper tool, and you can find them in the relevant
directories within your project's build output.

## Acknowledgments

ResChiper is inspired by the following projects and tools:

* [AabResGuard](https://github.com/bytedance/AabResGuard/)
* [AndResGuard](https://github.com/shwenzhang/AndResGuard/)
* [BundleTool](https://github.com/google/bundletool)

## License

[![Apache License v2.0 logo](artifacts/apache-licence-logo.png)](https://www.apache.org/licenses/LICENSE-2.0.txt)

    Copyright (C) 2023 goldfish07 (Ayush Bisht) <ayushbisht5663@gmail.com>
    This file is part of ResChiper.

    ResChiper is free software: you can redistribute it and/or modify
    it under the terms of the Apache License, Version 2.0 as published by
    the Apache Software Foundation, either version 2.0 of the License, or
    (at your option) any later version.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.