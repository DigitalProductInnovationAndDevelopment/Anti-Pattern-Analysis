# Code of Conduct and Commit Guidelines

## Our Pledge

We as members, contributors, and leaders pledge to make participation in our
community a harassment-free experience for everyone, regardless of age, body
size, visible or invisible disability, ethnicity, sex characteristics, gender
identity and expression, level of experience, education, socio-economic status,
nationality, personal appearance, race, caste, color, religion, or sexual
identity and orientation.

We pledge to act and interact in ways that contribute to an open, welcoming,
diverse, inclusive, and healthy community.

## Branch Naming Conventions

### Structure

Branch names should be descriptive and follow a consistent structure according to task type:

- feature/`<name>`.`<feature-name>`
- fix/`<name>`-`<feature-name>`
- experiment/`<name>`-`<feature-name>`
- refactor/`<name>`-`<feature-name>`

## Commit Message Guidelines

### Structure

Each commit message should consist of three parts:

1. **Header**
2. **Body** (optional)
3. **Footer** (optional)

### Header

The header should be a single line that includes a type, an optional scope, and a subject. The header should look like
this:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

- **type**: The type of change being committed (see below for accepted types)
- **scope**: The scope of the change (e.g., component or file name)
- **subject**: A brief description of the change

### Accepted Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc.)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing or correcting existing tests
- **build**: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
- **ci**: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
- **chore**: Other changes that don't modify src or test files
- **revert**: Reverts a previous commit

### Subject

- Use the imperative mood in the subject line (e.g., change, not changed or changes)
- Do not capitalize the first letter
- Do not add a period (.) at the end
- Keep the subject line to 50 characters or less

### Body

- Use the body to explain what and why vs. how
- Include motivation for the change and contrast with the previous behavior
- Use bullet points for clarity if necessary

### Footer

- Use the footer to reference issue trackers (e.g., "Closes #1234")
- Include breaking changes in the footer with the prefix BREAKING CHANGE

## Additional Resources

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Branching Naming Convention](https://nvie.com/posts/a-successful-git-branching-model/)
