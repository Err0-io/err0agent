# Contributing to err0agent

Thank you for your interest in contributing to err0agent! This document provides guidelines and instructions for contributing to the project.

## Commit Message Format

We follow [Conventional Commits](https://www.conventionalcommits.org/) specification for all commit messages. This helps us maintain a clean, readable commit history and enables automated tooling for changelog generation and semantic versioning.

### Format

Each commit message should follow this structure:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

Commits must use one of the following types:

- **feat**: A new feature for the user
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process, auxiliary tools, or maintenance tasks
- **ci**: Changes to CI/CD configuration files and scripts
- **build**: Changes that affect the build system or external dependencies (Gradle, dependencies)
- **perf**: A code change that improves performance

### Scope (Optional)

The scope provides additional contextual information about which part of the codebase is affected. Common scopes for err0agent include:

- **parser**: Language parsers and tokenization
- **core**: Core error code insertion logic
- **languages**: Language-specific implementations (java, python, go, etc.)
- **docker**: Docker-related changes
- **ci**: CI/CD pipeline changes
- **deps**: Dependency updates

Examples: `feat(parser):`, `fix(java):`, `chore(deps):`

### Subject

The subject line should:
- Use imperative, present tense: "add" not "added" nor "adds"
- Not capitalize the first letter
- Not end with a period
- Be no more than 72 characters

### Body (Optional)

The body should:
- Use imperative, present tense
- Explain **what** and **why** vs. **how**
- Wrap at 72 characters per line

### Footer (Optional)

The footer can contain:
- **Breaking changes**: Start with `BREAKING CHANGE:` followed by a description
- **Issue references**: `Refs #123`, `Fixes #456`, `Closes #789`
- **Co-authors**: `Co-Authored-By: Name <email@example.com>`

### Examples

**Simple feature:**
```
feat(parser): add support for Swift lambda expressions
```

**Bug fix with body:**
```
fix(java): resolve call stack depth calculation error

The previous implementation incorrectly counted anonymous inner classes
as separate call stack frames. This fix adjusts the logic to properly
handle Java anonymous classes and lambda expressions.
```

**Documentation update:**
```
docs: update README with Kotlin examples
```

**CI change:**
```
ci: add conventional commit validation to GitHub Actions
```

**Breaking change (using ! syntax):**
```
feat(core)!: change error code format from [ERR-123] to [ERR-0123]

All error codes now use zero-padded 4-digit format for consistency.
Existing error codes will need to be updated.
```

**Breaking change (using footer):**
```
feat(api): redesign error handler interface

BREAKING CHANGE: ErrorHandler.handle() now requires ErrorContext parameter
instead of separate code and message parameters.

Migration: Change ErrorHandler.handle(code, msg) to
ErrorHandler.handle(new ErrorContext(code, msg))

Refs #123
```

**Refactoring:**
```
refactor(parser): simplify token classification logic

Extract duplicate classification code into shared helper methods
to improve maintainability and reduce duplication.
```

## Tools to Help You Write Good Commits

### Local Git Hook (Recommended)

Install our commit message validation hook to get immediate feedback:

```bash
./scripts/install-commit-hooks.sh
```

This will:
- Install a `commit-msg` hook that validates your commits locally
- Configure a commit message template to guide you

The hook will reject invalid commits before they're created, saving you time.

### Commit Message Template

After running the install script, when you use `git commit` (without `-m`), you'll see a helpful template in your editor with examples and reminders.

To manually configure the template:

```bash
git config commit.template .gitmessage
```

### IDE Extensions

**VS Code:**
- Install the "Conventional Commits" extension by vivaxy
- Provides a GUI for creating properly formatted commits

**IntelliJ IDEA / Android Studio:**
- Install the "Conventional Commit" plugin
- Adds commit template and validation support

**Other IDEs:**
- Search for "Conventional Commits" in your IDE's extension marketplace

### Bypassing Validation (Not Recommended)

If you need to bypass local validation in exceptional cases:

```bash
git commit --no-verify -m "your message"
```

**Note:** CI validation will still run, so invalid commits will be caught before merging.

## Development Workflow

### Setting Up Your Environment

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Err0-io/err0agent.git
   cd err0agent
   ```

2. **Install commit hooks:**
   ```bash
   ./scripts/install-commit-hooks.sh
   ```

3. **Build the project:**
   ```bash
   ./gradlew build
   ```

4. **Run tests:**
   ```bash
   ./gradlew test
   ```

### Making Changes

1. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

2. **Make your changes** and write tests

3. **Test your changes:**
   ```bash
   ./gradlew test
   ./gradlew build
   ```

4. **Commit with Conventional Commits format:**
   ```bash
   git add .
   git commit
   # Your editor will open with the commit template
   # Or use -m flag with proper format:
   git commit -m "feat(parser): add new language support"
   ```

5. **Push your branch:**
   ```bash
   git push -u origin feature/your-feature-name
   ```

6. **Create a Pull Request** on GitHub

### Pull Request Guidelines

- Fill out the pull request template completely
- Ensure all commits follow Conventional Commits format
- Make sure all tests pass
- Update documentation if needed
- Reference any related issues

## Testing Guidelines

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "io.err0.client.test.Test0042JavaEnablePlaceholder"

# Run tests with verbose output
./gradlew test --info
```

### Adding New Tests

Tests follow a data-driven approach:

1. Create a numbered directory in `src/test/testdata/` (e.g., `test0123/`)
2. Add input files in subdirectory `01/`
3. Add expected output in `01-assert/`
4. Create a test class in `src/test/java/io/err0/client/test/` following the pattern `Test<number><Language><Feature>.java`

See existing tests for examples.

## Code Style

- Follow existing code style and patterns
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise

## Questions or Issues?

- Check existing [GitHub Issues](https://github.com/Err0-io/err0agent/issues)
- Create a new issue for bugs or feature requests
- Join the discussion on open issues

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

Thank you for contributing to err0agent! Your efforts help make error tracking better for everyone.
