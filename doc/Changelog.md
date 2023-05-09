# 0.2.0-SNAPSHOT

- Issue #4 - Use RuiGeneratedFragment instead of RuiFragment in generated classes
- run and success unit tests now generate code with and without trace
- change `unitTestMode` plugin option to `printDumps`
- add `jvmToolchain(11)` to projects
- make class `RuiDOMAdapter` open
- convert runtime manual block and branch test into a proper unit test

# 0.1.0

Works:

- sequence (components after each other)
- branches (if and when)
- event handlers
- automatic patch on state change

To be implemented in the near future:

- for loop
- higher order functions