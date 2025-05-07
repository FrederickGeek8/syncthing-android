# Maintenance
This document outlines the basic procedures for keeping the `syncthing-android` fork up to date with the [upstream `syncthing` binary core](https://github.com/syncthing/syncthing/releases). The procedure is, in fact, quite simple.

One of the advantages of this repository is that builds, including signing, can be completely automated. With an application such as [Obtainium](https://github.com/ImranR98/Obtainium) you can even have the `.apk` built by GitHub Actions automatically update on your Android phone.

This guide largely leverages that workflow and tries to minimize the amount of manually compiling that needs to be done.


## How the GitHub Workflows Works
The two main GitHub Action workflows that you should pay attention to on this repository are:

1. [`build-app.yaml`](/.github/workflows/build-app.yaml) which generates a debug `.apk` for non-tagged pushes to the `main` and `release branch`
2. [`release-app.yaml`](/.github/workflows/release-app.yaml) which _for a **tagged** commit to the **release** branch_:
   1. Compiles and signs a _release_ build of Syncthing-Android.
   2. Uploads the `.apk` to GitHub Releases as a _draft_ (meaning that you need to manually approve the release later).

## Procedures for Updating Syncthing-Android
In the event that there is a new [syncthing binary release](https://github.com/syncthing/syncthing/releases), you can perform the following steps to create a new version of the Android binary that utilizes this new core:

### Updating the code

After `cd`'ing to the root of this project in your _local development_ terminal, do:

1. Run `./script/update-syncthing.bash`. _Internally_ [that script](scripts/update-syncthing.bash):
   1. Checks for the latest stable _tag_ from the [syncthing release page](https://github.com/syncthing/syncthing/releases)
   2. Updates the [git submodule in `./syncthing/src/github.com/syncthing/syncthing`](/syncthing/src/github.com/syncthing) to point to that latest tag
   3. Creates a new git commit in this repository which says "Updated Syncthing to $LATEST_TAG".
2. Replacing `<version>` with the version of the new Syncthing binary (e.g. `1.29.6`) run: `./scripts/bump-version.bash --no-lint <version>`. _Internally_ [that script](scripts/bump-version.bash):
   1. Ensures that your build directory is clean, e.g. ensuring that you didn't forget to commit some changes.
   2. (**Optional**, disabled by `--no-lint`) Lints the code with `gradlew`. (See the "**Note 1**" "Notes" subsection below)
   3. Prompts you to write a changelog for this tag/release (utilizing your `$EDITOR` env variable)
   4. Updates the Gradle build information to reflect the proper, new version number.
   5. Creates a _tagged git commit_ which will be used to build the application in GitHub Actions.

### Building and publishing the app (via GitHub Actions)

There are two noteworthy git branches that you should pay attention to in this release workflow:
1. `main`
2. `release`, where the _release_ `apk`'s are built from.

If you are not already on the `main` branch after updating the Syncthing core and tagging your release, make sure to switch to that now with `git switch main`, and bring over/rebase your changes from your temporary branch onto main with `git rebase <my-temp-branch>`.

#### Step One: Building a debug version via GitHub Actions
Once you are on your main branch, running
```bash
git push -u origin main
```
will deploy your new code, but _not the git tags_ to your remote repository. As is explained in the above section regarding workflow, _pushing to the `main` branch will trigger a debug build_. This is fine and appropriate, and it is probably best to run a test build before attempting to push your git tag and build a release.

#### Step Two: Building your release `apk` on GitHub Actions
If you debug version on GitHub successfully compiled, and you're prepared to build a release version to go on your GitHub Release page, you can switch to your `release` branch and bring over your new changes & tags.
```bash
git switch release && git rebase main
```

If you now run the following command to push your new changes _and_ your newly tagged release to GitHub, the release `apk` of your app should be built and published as a draft to your `https://github.com/<username>/syncthing-android/releases` repository. 
```bash
git push -u --follow-tags origin release
```
If you approve the draft there, then the apk will be publically available!

You're done! You should now have a version of Syncthing-Android which has it's syncthing core updated to the latest upstream release.

### Notes, Gotchas, etc.
1. **Note 1:** This requires you to have a valid gradle environment. If you are [using Docker for development builds](/docker) (like I), then the `--no-lint` is useful for using my Git identity outside of the repository.

## Setting up the GitHub Action (for Building/Signing)
*This section is very incomplete.*

What I can tell you is that the GitHub Actions require 3 repository secrets defined:
1. `GNUPG_SIGNING_KEY_BASE64`
2. `SIGNING_KEYSTORE_JKS_BASE64`
3. `SIGNING_PASSWORD`

You can find out how to generate these values from other guides for building & signing Android apps in GitHub Actions with `keytool`. This section is incomplete and, for now, will remain incomplete.
