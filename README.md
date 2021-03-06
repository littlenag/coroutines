
![Coroutines](/coroutines-128-xmas.png)

# Scala Coroutines

[![Join the chat at https://gitter.im/storm-enroute/coroutines](https://badges.gitter.im/storm-enroute/coroutines.svg)](https://gitter.im/storm-enroute/coroutines?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[Coroutines](http://littlenag.github.io/coroutines)
is a library-level extension for the Scala programming language
that introduces first-class coroutines. 

This library directly builds on [Storm Enroute Coroutines](http://storm-enroute.com/coroutines).

# Enhancements

types of coroutines
* actor
* reactor
* transducer
* generator 
  - () -> (Y, _)  yield values, ignore return type, 
  - like iterator
  - implemented as enumerator right now
* worker 
  - () -> (Nothing, R)  yield no values, return type,
  - similar to kotlin coroutine
  - build in launch method? take scope?
  
A purely suspendable functions model doesn't have to worry about inferring a yield
type! Yielding can be done through a channel passed to the coroutine.

also means that yielding can be kept to things like generation sequences, iterators, like kotlin does

would want to be able to suspend with a hint
 - which channel or stream blocked on
 - if called sub-coroutine
 - if delayed
 - allow for open hints system?
 
streams - event source
channel - event sink
  
* stream algebra
 - next
 - yield
 - buffer
 - delay
 - hasPending, getPending
 - halt
 - fail
 - setState, getState
  
see scala-async-flow by phaller 

use Suspend[T] as type?

why not use a channel, like kotlin does, to let coroutines communicate?
 - because it requires a scheduler!
 - then this ends up just being a fancier Future, really not the goal

cross compile to kotlin coroutines?
 - not likely, this should be tailored for stream transformations

cross compile to a state monad?
 - for flink compat?

build same kotlin system for scala coroutines?
 - not likely

scalajs interop?
 - should at least try to compile to scalajs

kotlin interop?

compile to state machine vs control flow graph (cfg)?

performance?
 - get benchmarks working again
 - compare against
   - parsers
   - fn => (behavior, fn)
   - scala-async
   - kotlin
   - state monad

debugging?

scala-async
 - compile to state-machine?

dotty?
 - would require an entire rewrite

resume with value, like python coroutines?
 - working on it

kotlin launch and schedulers let coroutines act like futures
 - this would need to be part of a more general coroutines library

compile for comprehensions to while iterator where possible

zio interop?
 - good way to control effects
 
 
TODO
 - abstract over arity, probably use quill like semantics
 - coroutine constructor inside trait, specialize to particular yield and next values (stream in/out values)


# Misc

Check out the [Scala Coroutines website](http://storm-enroute.com/coroutines) for more info!

Service            | Status | Description
-------------------|--------|------------
Travis             | [![Build Status](https://travis-ci.org/storm-enroute/coroutines.png?branch=master)](https://travis-ci.org/storm-enroute/coroutines) | Testing only
Maven              | [![Maven Artifact](https://img.shields.io/maven-central/v/com.storm-enroute/coroutines_2.11.svg)](http://mvnrepository.com/artifact/com.storm-enroute/coroutines_2.11) | Coroutines artifact on Maven
