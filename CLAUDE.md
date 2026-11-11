# Divinity

Build commands, architecture and conventions are in [`.github/copilot-instructions.md`](.github/copilot-instructions.md).

## Combat restructure

The damage pipeline is being restructured across Codex, Divinity and Fabled. Before touching anything
under `manager/damage`, `manager/listener/object/VanillaWrapperListener.java`, or the Fabled hook, read:

- [`doc/COMBAT_RESTRUCTURE_PLAN.md`](doc/COMBAT_RESTRUCTURE_PLAN.md): the design, the decisions made, and the phased plan.
- [`doc/COMBAT_RESTRUCTURE_NOTES.md`](doc/COMBAT_RESTRUCTURE_NOTES.md): how to build and test, the test environment's gaps, and
  mistakes already made once.
