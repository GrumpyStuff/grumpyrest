# grumpyrest

grumpyrest is a Java REST server framework that does not use annotations, automatic dependency injection or reactive
streams, and minimizes the use of reflection. Instead,
* it leverages the Java type system to indicate the meaning of classes, fields and methods
* it calls constructors to create dependency objects, and passes constructor parameters to inject them
* it uses threads to achieve parallelism, and in particular virtual threads for highly parallel I/O

Particular differences with typical REST frameworks:
* To mount an endpoint to a URL, you do not annotate the handler method. Instead, you call a mount method and pass the
  handler as a parameter.
* To access request properties such as URL path parameters, querystring parameters or the request body, you do not
  declare handler parameters. Instead, you call a method. This method takes a specification of the expected type
  and performs validation before returning its data.
* Request/response bodies are typically expected to be JSON. To declare how the request body is parsed from JSON, as
  well as validations to perform, you use appropriate Java types that validate their parameters / fields in their
  constructor. Similarly, to declare how the response objects are mapped to JSON, you use appropriate Java types with
  a well-defined JSON mapping. Mappings in both directions can be registered from outside the data-holding classes to
  support existing classes.
* This includes nullable and optional fields: Appropriate generic wrapper types are provided to define nullable and/or
  optional fields. Java's null references are NOT considered equivalent to JSON-null nor to absent JSON properties,
  and having null references on the Java side of JSON mapping is considered an error to avoid any ambiguities.
* No type / field is nullable or optional by default. No default values will be inserted for missing fields. No
  coercion from number to string or from string to number will be done by default. An empty JSON object, empty JSON
  array, false, 0, null and missing fields are all different things that are not considered equal. If you want to
  bend these rules for specific types or fields, use an appropriate type for that which defines a relaxed mapping.
  If you want to bend these rules for all of your JSON mapping, replace the built-in type adapters by your own which
  define a relaxed mapping.

## What about performance?

It _is_ anticipated that grumpyrest may provide tools to tackle potential performance problems, which rely on
reactive programming and/or code generation. The goal of grumpyrest is not to totally avoid these things internally,
but to allow the developers using it to totally avoid them in their application code.

This means:
* We will not add anything like that to grumpyrest until it is perfectly clear that it solves a performance problem
  in modern Java which, for example, virtual threads cannot handle.
* None of this affects application code, unless you choose to use it in application code (because the performance
  problem lies there instead of inside grumpyrest).
* Any such features can be turned of to exclude them as a potential source of bugs, as well as to single-step debug
  into grumpyrest to understand what happens inside (e.g. to track down a specific bug). We all know what single-step
  debugging into highly asynchronous / reactive or generated code is like.
