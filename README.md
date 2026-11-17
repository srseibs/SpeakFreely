# MyProjectTemplate

A clean Android Studio template for starting new Android projects with consistent architecture, Gradle setup, navigation scaffolding, DI, and version catalog.

When a new repository is created from this template, project initialization happens automatically via a GitHub Action — requiring zero manual renames.

---

## How to Create a New Project From This Template

### 1. Create a new GitHub repo from this template
On the GitHub page for this template repo:
- Click **"Use this template"** → **"Create a new repository"**.
- **Repository Name**: Use **CamelCase with NO SPACES** (e.g. `TranslateFlow`, `KtorExample`, or `HeyBartender`).
  > **Important:** Do NOT include spaces or hyphens in the repository name. The automated setup requires CamelCase format to correctly generate your package namespace and spaced launcher app title.
- Click **"Create repository"**.

> **Note:** A GitHub Action runs automatically upon creation (~10 seconds) to configure your root project name, package namespace, and launcher app title based on your chosen repository name.

---

### 2. Clone the new repo locally in Android Studio
1. Open Android Studio.
2. Select **Get from VCS** (or **File → New → Project from Version Control...**).
3. **URL**: Paste the URL of your newly created GitHub repository (e.g. `https://github.com/srseibs/<YourProjectName>.git`).
4. **Directory**: Click the folder icon to select your target category parent folder (e.g., `...\AndroidStudioProjects\SailingHawkLabs\`), then ensure `\<YourProjectName>` is appended at the end of the path.
5. Click **Clone**. (Android Studio will create the `<YourProjectName>` subfolder automatically).

---

### 3. Open & Build
Android Studio will open your new project automatically. 
All project identifiers are automatically generated from your GitHub repository name:

| Identifier | How It Is Generated | Example (`TranslateFlow`) |
| :--- | :--- | :--- |
| **Gradle root project name (`settings.gradle.kts`)** | Same as repository name | `TranslateFlow` |
| **Package namespace** | Lowercase repository name | `com.sailinghawklabs.translateflow` |
| **Launcher app name (`strings.xml`)** | Spaced repository name | `Translate Flow` |
| **Git Remote (`origin`)** | Linked to your new GitHub URL | `https://github.com/srseibs/TranslateFlow.git` |

Perform an initial **Gradle Sync** in Android Studio and you are ready to build!

---

## Template Contents

- Automated project setup workflow (`.github/workflows/template-init.yml`)
- Version catalog (`gradle/libs.versions.toml`)
- Clean Gradle Kotlin DSL structure
- DI scaffolding
- Navigation scaffolding
- Compose baseline setup
