# Clint's LoamStream Experiment
## An experimental reformulation of Loamstream's core concepts. Provides:
* An API for constructing abstract pipelines that's compatible with Scala's for-comprehension syntactic sugar.
* A simplified view of abstract pipelines as a series of steps, lineraized from a DAG of dependencies, while also allowing steps to run in parallel.
* A (relatively) simple framework for adding new pipeline steps.
* The ability to compose new pipelines from existing ones by joining them together, linearizing sets of them, and applying other combinators, while preserving maximum type safety.
======
### Disclaimer
This is very much a strawman prototype - it skips some error handling and
includes some hacky code for invoking external commands - but it should
suffice as a proof-of-concept.

### Rationale
I was concerned that the current design makes defining pipelines particularly verbose, so that I have a hard time seeing what a pipeline is supposed to do from looking at the code that creates it.  What complexity Loamstream has may be purely necessary.  But if any of that complexity is accidental, we need to simplify things now to enable the work we'll need to do in the coming months.  This code exists to test the idea that a simpler formulation is possible.

### Summary
This code uses a library to provide a lot of the monadic machinery needed to allow building pipelines with a succinct, expressive API oriented to for-comprehensions, `map`, and `flatMap`.  Notably, none of that machinery needs to be written by the LoamStream team.  All we need to provide are:

1. A set of pipeline steps encoded as regular JVM classes
2. A function that maps each type of step to a value, performs side-effects, runs commands, etc.

With those two things, we can

1. Build up graphs representing a description of a computation to be performed (an abstract pipeline).
2. Examine those graphs, serialize them, transform them, or whatever - they're just data.
2. Execute that graph/description as many times as we want, with as many different runtime configurations as we want, at any time after the graph is built.

### Details
The core of my idea is to make a more succinct internal API for buildingabstract pipelines using for-comprehensions.  For example, in [SampleIdExtractionPipelineTest](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/test/scala/loamstream/SampleIdExtractionPipelineTest.scala) the main event is:

```scala
val pipeline: Pipeline[Pile.Set[String]] = for {
  path <- locate("classpath:mini.vcf")
  samples <- getSamplesFromFile(path)
} yield samples
```

Here, `Pipeline` (set out in [loamstream/package.scala](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/main/scala/loamstream/package.scala)) is an alias for the free monad implementation from the [Cats library](http://typelevel.org/cats/) (the jargon is largely irrelevant), parameterized on [loamstream.PipelineStep](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/main/scala/loamstream/PipelineStep.scala). `PipelineStep` represents a single step in an abstract pipeline, and `Pipeline` represents an abstract pipeline.

Or, `Pipeline[A]` is a *description* of some set of steps that will ultimately produce an `A`.  In the above example, the Pipeline will produce a `Pile.Set[String]`.  (`Pile.Set` is a placeholder class representing a Pile-like data structure backed by a Scala Set.  It's there to show that Pipelines can produce something kinda-sorta Loamstream-ish, nothing more.)

The above pipeline is made of two steps:

1. finding a file
2. getting sample ids from that file somehow

the methods `locate()` and `getSamplesFromFile()` are defined in [PipelineStep](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/main/scala/loamstream/PipelineStep.scala).

The PipelineStep companion object contains two sets of things: methods that produce Pipelines from various inputs (take a Path, return a Pipeline that produces a set of sample IDs, etc.) and case classes representing the actual steps in a pipeline.

The Cats library provides some magic to turn a PipelineOp[A] instance into something that can be mapped and flatMapped - that is, [something we can use in a for-comprehension](http://docs.scala-lang.org/tutorials/FAQ/yield.html).

A longer Pipeline that uses dummy external commands to simulate the pipeline I set up in Docker images for Intel is here: [ExternalCommandPipelineTest](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/test/scala/loamstream/ExternalCommandPipelineTest.scala) and contains the following pipeline definition:

```scala
val pipeline: Pipeline[Path] = for {
  fileA <- locate("src/test/resources/a.txt")
  fileB <- locate("src/test/resources/b.txt")
  combineResult <- runCommand("combine")(fileA, fileB)
  compressResult <- runCommand("compress")(combineResult.path)
  analysisResult <- runCommand("analyze")(compressResult.path)
} yield analysisResult.path
```

Note that the for-comprehensions don't actually *run* anything; they just build up a sequence of steps.  To run an abstract pipeline, you need a way to map those steps to concrete, possibly side-effecting actions, a [Mapping](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/main/scala/loamstream/Mapping.scala):

The gist is that a mapping is a function that takes a `PipelineStep[A]` and returns an `A`, however that should happen: resolving a path name, running an external command, etc.

From a Mapping we can make a [Runner](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/main/scala/loamstream/Runner.scala), which is just a thing that takes a `Pipeline[A]` and returns an `A`, by evaluating all the steps in the `Pipeline` using the `Mapping`.  This is what happens in the line

```scala
val analysisResult = pipeline.runWith(Mapping.fromLoamConfig(config))
```

in [ExternalCommandPipelineTest](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/test/scala/loamstream/ExternalCommandPipelineTest.scala).

### Cons
This approach is geared toward sequential computations.  It's possible to have steps run in parallel (see `parallelize` in [loamstream/Pipeline.scala](https://github.com/ClintAtTheBroad/loamstream-strawman/blob/master/src/main/scala/loamstream/Pipeline.scala)), but that's something that needs to be added on.

The API provided by Cats is geared toward walking a graph of pipeline steps, not examining the graph in total.  This could make some optimizations harder, if that was something we ever wanted to do.

The code looks nice (if I may say so), but the underlying ideas in Cats are advanced and hyper-abstract.  This could pose a problem if we needed to dig into that code in the future.  On the other hand, an analogy with other frameworks is possible: for example, if I want to respond to HTTP requests in a Java program, I can extend the HttpServlet class and implement some methods.  That the environment in which my class will run - a complex, multi-threaded app server that handles all corner cases of a decades-old protocol - is complex is largely of no consequence because I don't need to care about the implementation of the containing app server.

======

### Aside
The code to handle external commands is config-driven but is extremely rough and hacky.  This is an area in which we should look for as much library support as we can get: invoking commands is always tricky, and scala.sys.process, while convenient, exposes a few leaky abstractions.

### Summary
I was pleased to see that there is an existing pattern implemented in a well-engineered library that does (basically) what Loamstream aims to do - build descriptions of computations, map those descriptions to actual runnable things, and run them - with basically the same abstractions as the existing code (abstract pipeline => `Pipeline`, tool mapping => `Mapping`, `Executor` => `Runner`).


