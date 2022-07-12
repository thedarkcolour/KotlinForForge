## Kotlin for Forge Maven
Hello there! This here is the Maven repository holding every published version of KFF.
It is not a traditional Maven, here are the steps to publishing a new version:

1. Compile a version of KFF using one of the main branches
2. Create a folder matching the version of the compiled jars in `thedarkcolour/kotlinforforge/`
3. Paste the `kotlinforforge-x.x.x.jar` and `kotlinforforge-sources-x.x.x.jar` JARs from the 
  earlier compilation into the new folder
4. Copy over the `web.html` and `kotlinforge-x.x.x.pom` files from another version's folder,
  renaming the POM file to the correct version
5. Replace all occurrences of the old version number in `web.html` and `kotlinforforge-x.x.x.pom`
  with the new version. If the version does not match in the POM file, Maven will not find it
6. Replace the transitive library versions in the POM file with the versions matching the KFF version
7. Add the version as an entry in `thedarkcolour/kotlinforforge/maven-metadata.xml` as well as
  in the `thedarkcolour/kotlinforforge/web.html` file
8. Run the Java file at `src/main/java/thedarkcolour/kotlinforforge/GenerateWebsite.java`
  to generate all the checksums for the newly added/changed files
9. Add any changed/new files and commit them to Git, then push to the remote website branch!

[I am open to any improvements to this process :)](https://discord.com/invite/tmVmZtx)