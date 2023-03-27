# Number Server
Simple socket based webapp that opens socket connections to clients, allowing them to connect and send numbers to the
server for logging purposes.

## Requirements
All requirements are printed upon running the helper scripts.
- `maven`
- `java` (at least 11)

## Running
### Manually
Server can be started via `./runServer.sh`
- `-d` param opens up the debug port on the server

Clients can be started via `./runClient.sh`
- `user` param runs the client with "user" input. That is it expects inputs to come in via stdin.
- `infinite` param runs the client and generates infinite random numbers to feed to the server
- `count <x>` increments upwards starting at `x`

All building is done via maven and is auto-compiled on every run. If you'd prefer not to do this set the environment
variable `SKIP_COMPILE` to any non-blank value.

### Via tests
Tests are not yet implemented.

## Missing Implementations
- [ ] Tests, especially those covering:
  - [ ] multi-threading
  - [ ] client-server communication
  - [ ] Large data sets
- [ ] Support for large data sets reaching the max 1,000,000,000 entries
  - [ ] Technically the current solution can support that, given our JVM sizing, but perforamnce tanks
        around the 100 million entries mark.This could possibly be solved by splitting our set into multiple
        "prefixed-buckets" where each set is only responsible for numbers starting with `000`, `001`, `002`, etc.

## Assumptions
- The default backlog implementation of the `ServerSocket` class is sufficient for backlogged clients
- Accepting a connection from a client (via `serverSocket.accept()`) is okay even if we have >5 clients connected
  - Both of these assumptions could be fixed by changing the logic around the backlog (setting the `serverSocket`
    backlog param to `0`) or creating a semaphore to count how many concurrent connections there are and block until we
    have an available permit.
- It's okay the client is not the most robust. It functions but there are a fair number of improvements around error
  handling.
- We have at least 5gb of free memory to work with.
  - If we don't have that available for whatever reason, there are ways we can offload the duplicate detection
  - This also means we are running on a 64bit machine

### Performance Results
Running on a Intel i7-9700K with 16gb ram results in a peak of around 23 million numbers per 10 second reporting period