# Vinyl  [![CircleCI](https://circleci.com/gh/UrbanCompass/vinyl.svg?style=svg&circle-token=a8cb778fca5ae22550cac7d9a394808114a3feed)](https://circleci.com/gh/UrbanCompass/vinyl)
A lightweight library to record &amp; replay execution data from various code flows. Here are some basic problems this library is trying to solve:
  - Make integration tests perform at the speed for unit tests (narrow the gap)
  - Improve developer productivity by speeding up local dev/testing cycles that rely on external resource
  - Avoid writing mocks that just needs to mimic a service behavior
  
## How does Vinyl solve the above problems?
Vinyl can directly be integrated into places that are sources of problem. Once Vinyl is integrated, it records the output/response from the underlying call (be it network or datbase or any other slow external resource) and it plays back the recorded data when the same inputs are passed on. This vastly improves the responsiveness of the operation as it short circuits the actual slow operation.

This library has been integarted for network calls in mobile development/testing. Has been a boon for reducing the execution times for backend services that pull data from database & other service calls. It has also made most of the mocks unnecessary as playback is going to act as a mock and provide the actual response stored earlier.

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
Like Vinyl music device (from which this library took inspiration from), Vinyl requires a player to record & playback the data. The player also needs to know what serilization format is used in storing/retrieving the data. Right now, Vinyl has support for disk based player & JSON serialization. Here is how to initialze the vinyl library:
```kotlin
val serializer: Serializer = JSONSerializer.getInstance()

val player: RecordPlayer = LocalFileSystemRecordPlayer()
    
val vinyl: Vinyl = Vinyl.Builder().usingMode(Mode.RECORD)
            .usingRecordingConfig(RecordingConfig(serializer, "/tmp/vinyl"))
            .withPlayer(player)
            .create()
```
