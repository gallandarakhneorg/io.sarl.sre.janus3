# Janus version 3

Janus is the official SARL run-time environment.

The third version of Janus is completely rewritten in SARL programming language,
and provides specific implementation optimizations.

## Major differences between Janus 3 and Janus 2

The major differences with the version 2 (current official version) are:

1. Implementation language: **SARL** instead of Java
2. Use of the **Bootique framework** to make easier the booting process and the module extension by third parties.
3. **Simplified framework** architecture
   * `Context` becomes an `AgentTrait`
   * New implementation of the agent's lifecycle support (`AgentLife` and `BehaviorLife`)
   * New implementation of the Janus's event firing mechanism
   * New implementation of the execution service
   * Implementation of **namespaces**
   * Implementation of a **probing mechanism**
   * Network related features are removed in order to be proposed by a Janus extension
4. Optimization of the code to be faster.
5. Ready for a [simulation oriented extension](https://github.com/gallandarakhneorg/io.sarl.sre.extensions.janus3-simulation.git)


Contact:
Stéphane Galland <stephane.galland@utbm.fr>
