# ![Vinyl](vinyl.png) Vinyl  ![kotlin](https://img.shields.io/badge/kotlin-compatible-success?logo=kotlin&style=flat) [![CircleCI](https://circleci.com/gh/UrbanCompass/vinyl.svg?style=svg&circle-token=a8cb778fca5ae22550cac7d9a394808114a3feed)](https://circleci.com/gh/UrbanCompass/vinyl) [![codecov](https://codecov.io/gh/UrbanCompass/vinyl/branch/master/graph/badge.svg?token=1OJUZZ00ZA)](https://codecov.io/gh/UrbanCompass/vinyl) [![](https://jitpack.io/v/UrbanCompass/vinyl.svg)](https://jitpack.io/#UrbanCompass/vinyl)
A lightweight library to record &amp; playback execution data across code flows. Here are some basic problems this library is trying to solve:
  - Double up as a cache for network boundaries between systems
  - Make integration tests perform at the speed for unit tests (narrow the gap)
  - Improve developer productivity by speeding up local dev/testing cycles that rely on external resource
  - Avoid writing mocks that just needs to mimic a service behavior
  
## How does Vinyl solve the above problems?
Vinyl can directly be integrated into places that are sources of problem. Once Vinyl is integrated, it records the output/response from the underlying call (be it network or database or any other slow external resource) and it plays back the recorded data when the same inputs are passed on. This vastly improves the responsiveness of the operation as it short circuits the actual slow operation.

This library has been integrated for network calls in mobile development/testing. Has been a boon for reducing the execution times for backend services that pull data from database & other service calls. It has also made most of the mocks unnecessary as playback is going to act as a mock and provide the actual response stored earlier.

## What was the impact after integrating Vinyl?

### Integration at the speed of Unit Tests
Integration tests typically have long execution times as it accesses external resources for loading data. This poses a huge challenge, if we have to run integration tests more frequently (as frequent as the unit tests). With Vinyl recording the data for external interactions, it acts as a stub playing back the recorded data for the given inputs. This makes the integration tests run faster by multifold.

### Developer Productivity
Vinyl was integrated with Android App with the capacity to record all network calls. Working from remote location, we had a higher latency (>100ms) to most of the apis and subsequently accessing all screens in mobile takes more time due to the high network latency. Post integration, the development iterations are faster as screens respond in a fraction of the second.

### Test Stability
If the dependent services are down due to restarts or other external issues, we would have the concern of flaky integration tests. With Vinyl, we avoid the dependency on external resources altogether thereby improving the stability and reliability on our integration tests.

## How to integrate Vinyl into existing code?
Below is a generic mechanism to integrate the library into existing codebase.

#### Kotlin:
```kotlin
val source = "source class or service name"
val method = "method name or service method end-point"
val input1 = Data("param-1 name", param1)
val input2 = Data("param-2 name", param2)

// Try to get play the recorded data using vinyl
val recordedScenario = vinyl.playback(Scenario(source, method, listOf(input1, input2)))

// Use the playback data
if (recordedScenario != null) {
    return recordedScenario.output.value as Type
} 

// Record the data as it is a new scenario
else {
    output = chain.proceed()
    val scenario = Scenario(source, method, Arrays.asList(input1, input2), output)
    vinyl.record(scenario)
    return output
}
```

#### Java:
```java
String source = "source class or service name";
String method = "method name or service method end-point";
Data input1 = new Data("param-1 name", param1);
Data input2 = new Data("param-2 name", param2);

// Try to get play the recorded data using vinyl
Scenario recordedScenario = vinyl.playback(new Scenario(source, method, Arrays.asList(input1, input2)));

// Use the playback data
if (recordedScenario != null) {
    return (Type)recordedScenario.getOutput().getValue();
} 

// Record the data as it is a new scenario
else {
    Type output = chain.proceed();
    Scenario scenario = new Scenario(source, method, listOf(input1, input2), output);
    vinyl.record(scenario);
    return output;
}
```

## How to create an instance of Vinyl?
Like Vinyl music device (from which this library took inspiration from), Vinyl requires a player to record & playback the data. The player also needs to know what serialization format is used in storing/retrieving the data. Right now, Vinyl has support for disk based player, database based player(using [rocksDB](https://github.com/facebook/rocksdb)) & JSON serialization. Here is how to initialize the vinyl library:
#### Kotlin:
```kotlin
val serializer: Serializer = JSONSerializer.getInstance()

//To use file based player
val fileBasedPlayer: RecordPlayer = LocalFileSystemRecordPlayer()

//To use database based player
val databaseBasedPlayer: RecordPlayer = DatabaseRecordPlayer()
    
val vinyl: Vinyl = Vinyl.Builder().usingMode(Mode.RECORD)
            .usingRecordingConfig(RecordingConfig(serializer, "/tmp/vinyl"))
            .withPlayer(fileBasedPlayer)
            .create()
```
#### Java:
```Java
Serializer serializer = JSONSerializer.getInstance();

//To use file based player
RecordPlayer fileBasedPlayer = new LocalFileSystemRecordPlayer();

//To use database based player
RecordPlayer databaseBasedPlayer = new DatabaseRecordPlayer();

Vinyl vinyl = new Vinyl.Builder().usingMode(Mode.RECORD)
            .usingRecordingConfig(new RecordingConfig(serializer, "/tmp/vinyl"))
            .withPlayer(databaseBasedPlayer)
            .create();
```

## How to include vinyl as dependency

#### Maven:
```Maven
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.UrbanCompass</groupId>
    <artifactId>vinyl</artifactId>
    <version>2.0</version>
</dependency>
```

#### Gradle:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.UrbanCompass:vinyl:2.0'
}
```
