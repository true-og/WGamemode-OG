# WGamemode 3

A spigot plugin that allows server admins to set automatic gamemode rules for WorldGuard regions. 

When a player enters a WGamemode-managed region, their gamemode will be automatically 
updated to the region's set gamemode, and when they leave, they will return to their 
original gamemode.

This is an evolution of the original [WGamemode plugin](http://dev.bukkit.org/bukkit-plugins/wgamemode/) written by Sinnoh in 2012.

It was later updated by [soren121](https://github.com/soren121/wgamemode) in 2015.

Most recently it was updated by NotAlexNoyle for [TrueOG](https://trueog.net/) in 2023.

Supports: 1.18.2

Licensed under the LGPLv3 (or any later version.) See LICENSE.txt for more information.

## Usage

This plugin requires that you install WorldEdit 7+ and WorldGuard 7+ on your server.  
The only supported version is 1.18.2. Though it might work with earlier versions too.

**Warning: Adding multiple regions that overlap may result in undesired/undefined 
behavior.** Do not add a region that overlaps another added region.

### Configuration

You don't have to manually configure any settings in the plugin's `config.yml` 
file, but you can if you want.

This plugin has three settings:

 * *regions*: This is an associative array of WorldGuard regions that WGamemode 
   should manage. The key is the region name, and the value is the gamemode that 
   region should have.
   
   For example:
   ```
   regions:
     townsquare: "adventure"
     cathedral: "creative"
   ```
 * *stopItemDrop*: Boolean (true/false). If true, prevents item drops in 
   WGamemode-managed regions. Default is false.
 * *announceGamemodeChange*: Boolean (true/false). The plugin will tell players 
   when they are entering or exiting a WGamemode-managed region. Default is true.
   
An example is provided [here](https://github.com/soren121/wgamemode/blob/master/src/main/resources/config.yml).
That same example is also automatically generated in your server's `plugins` 
directory when the plugin is run for the first time.

### Commands

WGamemode has two in-game commands that do what you expect:

| **Command** | **Permission** | **Description** |
|-----------|------------------|------------------------------------------------|
| /wgadd [region] [gamemode] | wgamemode.add | Adds a region to WGamemode's region list. |
| /wgremove [region] | wgamemode.remove | Removes a region from WGamemode's region list. |
 
## Building from source

To build this plugin from source, you'll need to install OpenJDK 17.

#### To compile:  
 
    $ gradlew build
    
Your JAR file will be in the `build/libs` directory.
