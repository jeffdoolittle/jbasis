# Release Notes

## GH-6

Validation interceptors.

## GH-11

Fix method overloading resolution.

Currently the InterceptionInvocationHandler fails if the
proxied service has overloaded methods (methods with the
same name).

## GH-10

Fix minor logging issues.

## GH-8

Prevent creating a new scope from a scoped container.

## GH-4

Automatic dependency resolution.

## GH-2

Implement scoped container.

## GH-1

Implement basic IoC and interception.

Basic inversion of control container implementation with
Singleton and Transient lifetime handling.

Interception implementation using java.lang.reflect.Proxy
to add intercept behavior.
