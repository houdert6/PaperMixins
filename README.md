# PaperMixins
PaperMixins is a Paper plugin and Mixin loader which allows adding Mixins to your PaperMC servers

## Installing and configuring PaperMixins
Install PaperMixins just like you would any other plugin. Upon starting your server, PaperMixins will generate a `config.yml` and `mixins` folder. You must then read `config.yml` and configure your server arguments (under most circumstances, uncommenting the example in the config is fine). After this is done, PaperMixins will load any mixins you add to your `mixins` folder on server startup.

/reload will not reload mixins and you must restart your server to apply any changes.

## Creating a Mixin
Note that this README will *not* be a tutorial on how to code a mixin, there's already plenty of other resources out there on that. This README will just explain how to made a Mixin mod for PaperMixins.

You can create a mixin mod for PaperMixins in two ways:
- Creating a mixin mod from scratch as explained below
- Cloning the Example Mixin [to be added]

### Creating a Mixin mod from scratch
To get started, you can create a new Java project depending on the Paper API. If using IntelliJ IDEA with the Minecraft Development plugin, you can create a new Paper plugin, and then delete the automatically generated plugin.yml and starter classes.

After creating a project, you should add [Fabric Mixin](https://github.com/FabricMC/Mixin) as a dependency. If using Maven, add the following to your `pom.xml`
```xml
<!-- Fabric maven repo -->
<repository>
    <id>fabric-repo</id>
    <url>https://maven.fabricmc.net/</url>
</repository>

<!-- Fabric Mixin dependency -->
<dependency>
    <groupId>net.fabricmc</groupId>
    <artifactId>sponge-mixin</artifactId>
    <version>0.17.0+mixin.0.8.7</version>
    <scope>compile</scope> <!-- Will be updated to provided, check the note below -->
</dependency>
```
NOTE: In a future commit to PaperMixins, Fabric Mixin will be correctly loaded by the PaperMixins ClassLoader. At that point, the `<scope>` of the dependency in this will be updated to `provided` instead of `compile`, so watch out for that!

Next, you should add a `papermixins.yml` to your resources:
```yml
name: Name of your mixin mod
description: Describe your mixin mod
author: your name here
link: https://example.com
related-plugin: YourPlugin # If this mixin mod is related to a plugin somehow
version: 1.0.0
mixins: yourmixin.mixins.json # Should have a unique name
```
Only the name and mixin file are required

Now, add your mixin configuration file. A sample Mixin config could look something like
```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "your.package",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "YourMixin"
  ],
  "server": [],
  "injectors": {
    "defaultRequire": 1
  },
  "overwrites": {
    "requireAnnotations": true
  }
}
```
For a bit more info, you can check out [the fabric Mixin registration wiki page](https://wiki.fabricmc.net/tutorial:mixin_registration), just ignore the part about `fabric.mod.json`.

Finally, you can go code your mixins. 

## How is PaperMixins different from other PaperMC Mixin loaders? Why should I choose it?
PaperMixin is a Paper plugin that doesn't require you to change your server's startup command, and it runs in the same `java` process that you started. It's also entirely not based at all on the code of any existing Paper mixin loaders, mostly being written from scratch while taking inspiration from Fabric's implementation.

It's up to you what Mixin loader you choose. Pick whatever works best for your server.
