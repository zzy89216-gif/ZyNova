# Installing from favorites

## One-tap download into the current instance

When you reach the resource pages from "Version settings → Resources → Download resources", the favorites page carries that instance's context:

- A download button appears at the right of each card; one tap downloads and installs the resource into that instance.
- Required dependencies (for example a mod's required libraries) are downloaded and installed together, so you do not have to hunt them down.
- If a required dependency really cannot be found, the app says exactly what is missing instead of pretending the install succeeded.

Entered from the home screen's resource center there is no instance context, so the download button installs into the **current instance**; to target a different one, come in through that instance's "Version settings → Resources". Tapping the card itself still opens details (type, size, checksum).

## Local archive

- With an instance context, "Install" copies straight into that instance (also filling in recognizable dependencies) without asking for an instance.
- Without a context, the instance list is still shown.
- Files that cannot be identified online are copied directly and dependencies must be added manually.
