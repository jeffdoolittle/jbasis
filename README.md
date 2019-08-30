# jbasis

Java library for building Aspect Oriented business applications at lightning speed.

## Tests

To continually run the tests when there are changes to any files:

`gradle -t test`

To view test output:

`live-server --port=8181 ./build/reports/tests/test`

## JavaDoc

To build the javadocs

`gradle javadoc`

To view the java docs:

`live-server --port=8282 ./build/docs/javadoc`

## Sonarqube

This project uses Sonarqube to validate test coverage and code quality.

`gradle sonarqube --no-daemon`

By default, Sonarqube runs at `http://localhost:9000`
