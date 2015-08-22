#FlashBus
The **fastest Android Event Bus on Earth**. No boilerplate code required as the event bus is custom generated in compile time. This makes it significantly faster than any
other generic event bus implementation.

##Features

FlashBus is a machine generated Android event bus customized 100% to the host Android application

* the event bus code is **auto generated** in compile time
* the customized code runs **significantly faster** than any generic Android event bus solution (see benchmark results or run FlashBusBenchmark application on your device)
* the event bus **size is trimmed** to your application (depends on how you use it in your code, ~10k for an average application)
* makes your code really simple (the boilerplate code is auto generated)

FlashBus is really the **next generation technology**

* unlike generic solutions, **FlashBus really knows how your code works and fits perfectly** to it (no hash tables, no mappings)
* unlike generic solutions, **FlashBus does contain the minimum code needed to your application** (no wrappers, no conversions, no crazy magic)
* **integrates perfectly with your code** (anyway, it is built exactly to your code)
* amazingly fast and optimized (less code means less memory footprint and faster execution)
* it is designed exclusively for Android (backward compatible to API1, minimal GC pressure, minimal memory footprint, lightweight API)

FlashBus has the features you already know and used before

* **familiar, easy to use** API
* switching between threads
* sticky events

##Add FlashBus dependency to your project
FlashBus is available on Maven Central and JCenter. Add the following Gradle dependency to your project module(s) with which you want to use FlashBus.
**Always use the last version** of FlashBus (check for the last version [here](CHANGELOG.md)).

```groovy
    compile 'com.msagi:flashbus:1.1.0'
```

so in case of a generic Android application, the 'dependency' section of the 'build.gradle' file which uses FlashBus looks something like this
```groovy
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:22.2.1'
    compile 'com.msagi:flashbus:1.1.0'
}
```

##Insert code to build.gradle in the main project module
Insert this code snippet to the end of your build.gradle file in your main project module (this will have effect on all the sub-modules in your project too).
The code snippet integrates FlashBus with your Gradle Android Application project.

```groovy
File flashBusOutputDir = new File("build/generated/source/flashbus")
android.applicationVariants.all { variant ->
    String variantSubDir = variant.dirName
    File flashBusOutput = new File(flashBusOutputDir, variantSubDir)
    variant.addJavaSourceFoldersToModel(flashBusOutput);
    variant.javaCompile.doFirst {
        flashBusOutput.mkdirs();
    }
    variant.javaCompile.options.compilerArgs += [ "-s", flashBusOutput.getAbsolutePath() ]
}
```

so in case of a generic Android application, the build.gradle file with FlashBus looks something like this
```groovy
apply plugin: 'com.android.application'

android {
    compileSdkVersion 22
    buildToolsVersion "22.0.1"

    defaultConfig {
        applicationId "com.msagi.myapplication"
        minSdkVersion 14
        targetSdkVersion 22
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:22.2.1'
    compile 'com.msagi:flashbus:1.1.0'
}

File flashBusOutputDir = new File("build/generated/source/flashbus")
android.applicationVariants.all { variant ->
    String variantSubDir = variant.dirName
    File flashBusOutput = new File(flashBusOutputDir, variantSubDir)
    variant.addJavaSourceFoldersToModel(flashBusOutput);
    variant.javaCompile.doFirst {
        flashBusOutput.mkdirs();
    }
    variant.javaCompile.options.compilerArgs += [ "-s", flashBusOutput.getAbsolutePath() ]
}
```

##Developers Guide
Detailed description on how to use FlashBus is available in the [Developers Guide](HOWTO.md).

##License
FlashBus binaries and source code can be used according to the [Apache License Version 2.0](LICENSE).
