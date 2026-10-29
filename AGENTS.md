# Agent instructions

Follow `../EmptyPlugin/AGENTS.md` for the shared rules.

## Repo-specific rules

- `CommandSneakyLuckPerms` compiles user regexes and uses `matches()` (full-string). Catch `PatternSyntaxException` and tell the sender, and use `.*` explicitly in docs and examples.
- Modify nodes only through the LuckPerms API (`user.data()`) and persist with `userManager.saveUser`. Never edit LuckPerms files or run `/lp`.
- The command is admin-only (`sneakyluckperms.command.sneakylp`); never expose regex removal to players.
- Keep the `CMI-Fake-Operator` exclusion in online-player tab completion.
