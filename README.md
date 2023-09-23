## Problem Statement

Please create a variable data evaluation that is to be supplied with a data file
and an instruction file and generates an output file. All files are in a Json
format.

The structure of the files is not given by a schema. It can be derived from the
example files and is also described below.

The input data is in a simple format: Each file contains a root object with one
member `entries`. It defines a list of data points and each data point can
contain a variable number of members with child objects. In our example, each
data point is a city and has three members. One member is mandatory, this is
called `name` and is used for filtering.

The operations are similar in structure. The file contains an object with the
member `operations` which defines a list of operation objects. Each operation always
has these four members:

- `name` – The name to be used for the output.
- `function` – The function to be evaluated, this can be `min`, `max`, `sum` or `average`.
- `field` – An array of member names to access the value for the operation.
- `filter` – A regular expression to be applied to the `name` member. Only
  entries matching the regular expression should be included in the evaluation.

The output also consists of a list of objects which contain the operation name
and the formatted calculated value. Floating point numbers are to be written
with exactly two decimal places.

Attached you will find three example files that perform such an evaluation.

We ask for an implementation on the Java VM; you may use Java, Kotlin, Groovy or
Scala as programming language.

All standard libraries of the language you are using may be used, but no others.
An exception is a library to handle JSON files (for example [Jackson
library](https://github.com/FasterXML/jackson-databind)). If you use an external
dependency, use your favorite build tool (like Gradle, Maven, ...). Possible
other exceptions are unit testing libraries (like JUnit, TestNG, etc.).


## Files provided

The following files are supplied to solve the problem:

- `data.json` – Example of input data.
- `operations.json` – Example of operations to be calculated.
- `output.json` – The results for the sample data provided.
- `JsonCalculator.java` The entry point for the implementation


