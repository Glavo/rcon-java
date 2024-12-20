# RCON Java

[![Gradle Check](https://github.com/Glavo/rcon-java/actions/workflows/check.yml/badge.svg)](https://github.com/Glavo/rcon-java/actions/workflows/check.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/org.glavo/rcon-java)](https://search.maven.org/artifact/org.glavo/rcon-java)

A Java Minecraft RCON client, 
it based on [rkon-core](https://github.com/Kronos666/rkon-core/).

This project can be used as a command line application to remotely connect to the Minecraft server, 
and can also be imported by other Java projects as a library.

## Usage

### Use it as a library

```java
import org.glavo.rcon.Rcon;

// Connects to 127.0.0.1 on port 27015
Rcon rcon = new Rcon("127.0.0.1", 27015, "mypassword");

// Example: On a minecraft server this will list the connected players
String result = rcon.command("list");

// Display the result in the console
System.out.println(result);
```

### Use it in console

RCON Java can be run directly in the console:

```
PS> java -jar rcon-java.jar
The server IP or domain name (default 127.0.0.1): ***.***.**.***
The RCON network port (defaule 25575):
The password for RCON:

RCON> list
There are 0 of a max of 25 players online:

RCON> help
/advancement (grant|revoke)/attribute <target> <attribute> (base|get|modifier)/ban <targets> [<reason>]/ban-ip <target> [<reason>]/banlist [ips|players]/bossbar (add|get|list|remove|set)/clear [<targets>]/clone <begin> <end> <destination> [filtered|masked|replace]/data (get|merge|modify|remove)/datapack (disable|enable|list)/debug (report|start|stop)/defaultgamemode (adventure|creative|spectator|survival)/deop <targets>/difficulty [easy|hard|normal|peaceful]/effect (clear|give)/enchant <targets> <enchantment> [<level>]/execute (align|anchored|as|at|facing|if|in|positioned|rotated|run|store|unless)/experience (add|query|set)/fill <from> <to> <block> [destroy|hollow|keep|outline|replace]/forceload (add|query|remove)/function <name>/gamemode (adventure|creative|spectator|survival)/gamerule (announceAdvancements|commandBlockOutput|disableElytraMovementCheck|disableRaids|doDaylightCycle|doEntityDrops|doFireTick|doImmediateRespawn|doInsomnia|doLimitedCrafting|doMobLoot|doMobSpawning|doPatrolSpawning|doTileDrops|doTraderSpawning|doWeatherCycle|drowningDamage|fallDamage|fireDamage|forgiveDeadPlayers|keepInventory|logAdminCommands|maxCommandChainLength|maxEntityCramming|mobGriefing|naturalRegeneration|randomTickSpeed|reducedDebugInfo|sendCommandFeedback|showDeathMessages|spawnRadius|spectatorsGenerateChunks|universalAnger)/give <targets> <item> [<count>]/help [<command>]/kick <targets> [<reason>]/kill [<targets>]/list [uuids]/locate (bastion_remnant|buried_treasure|desert_pyramid|endcity|fortress|igloo|jungle_pyramid|mansion|mineshaft|monument|nether_fossil|ocean_ruin|pillager_outpost|ruined_portal|shipwreck|stronghold|swamp_hut|village)/locatebiome <biome>/loot (give|insert|replace|spawn)/me <action>/msg <targets> <message>/op <targets>/pardon <targets>/pardon-ip <target>/particle <name> [<pos>]/playsound <sound> (ambient|block|hostile|master|music|neutral|player|record|voice|weather)/recipe (give|take)/reload/replaceitem (block|entity)/save-all [flush]/save-off/save-on/say <message>/schedule (clear|function)/scoreboard (objectives|players)/seed/setblock <pos> <block> [destroy|keep|replace]/setidletimeout <minutes>/setworldspawn [<pos>]/spawnpoint [<targets>]/spectate [<target>]/spreadplayers <center> <spreadDistance> <maxRange> (under|<respectTeams>)/stop/stopsound <targets> [*|ambient|block|hostile|master|music|neutral|player|record|voice|weather]/summon <entity> [<pos>]/tag <targets> (add|list|remove)/team (add|empty|join|leave|list|modify|remove)/teammsg <message>/teleport (<destination>|<location>|<targets>)/tell -> msg/tellraw <targets> <message>/time (add|query|set)/title <targets> (actionbar|clear|reset|subtitle|times|title)/tm -> teammsg/tp -> teleport/trigger <objective> [add|set]/w -> msg/weather (clear|rain|thunder)/whitelist (add|list|off|on|reload|remove)/worldborder (add|center|damage|get|set|warning)/xp -> experience

RCON> exit
Bye bye!
```

## Download

[GitHub release page](https://github.com/Glavo/rcon-java/releases) provides pre-built jar files,
and native executable files that can be run directly without the Java runtime environment.

You can also add it as a dependency library to your project:

Maven:
```xml
<dependency>
  <groupId>org.glavo</groupId>
  <artifactId>rcon-java</artifactId>
  <version>3.0</version>
</dependency>
```

Gradle:
```kotlin
implementation("org.glavo:rcon-java:3.0")
```

## Especially thanks

<img alt="PLCT Logo" src="./PLCT.svg" width="200" height="200">

Thanks to [PLCT Lab](https://plctlab.org) for supporting me.

![IntelliJ IDEA logo](https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.svg)

This project is developed using JetBrains IDEA.
Thanks to JetBrains for providing me with a free license, which is a strong support for me.
