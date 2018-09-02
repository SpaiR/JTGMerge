[![Build Status](https://travis-ci.org/SpaiR/JTGMerge.svg?branch=master)](https://travis-ci.org/SpaiR/JTGMerge)
[![License](http://img.shields.io/badge/license-MIT-blue.svg)](http://www.opensource.org/licenses/MIT)

# JTGMerge

## About

CLI-based app for .dmm files to improve mapping experience.

Basically, it's an evolution of original [JMerge](https://github.com/Baystation12/JMerge) tool,
but unlike it JTGMerge is able to properly work with `TGM` format and has number of additional features like:

- Full sorting of atoms and there variables in tiles definition. This feature is just for pretty print,
  but consistency is consistency. Sorting order is next:
    * Movable objects -> turfs -> areas
    * Variables are sorted by names with natural order
- Detecting and removing of repeatable content definitions. Because of other tools are working with map data
  in a string way, tile presets like `"a"=(/obj{a=1;b=2})` and `"a"=(/obj{b=2;a=1})` are not same for them.
  JTGMerge avoids this problem by handling data like separate objects.
- Converting from classic byond format to tgm and vice versa.  

Other features are mostly migrated from JMerge:
- Map cleaning, so map changes made by DreamEditor will be minimized in final diff.
- Interactive conflict resolver. It fully works, but has one unresolved problem.
  For some reason while rebasing console process doesn't provide input stream, so user is unable to make choice which version should be used.
  Quick fix for this problem is to start JTGMerge in separate terminal or don't use `git rebase` if you are sure that conflict will occur.
  
#### Multi-Z restriction!

JTGMerge does not support multi-z map files in any form. Almost every codebase already divided Z-levels in separate files,
so this feature is redundant and just would add unneeded complexity to the app.

## How To Use

The only requirement for usage is to have installed Java at least 8 version or higher.<br>
Usage example: `java -jar JTGMerge.jar clean originalMap.dmm backupMap.dmm outputMap.dmm --tgm false`

Commands guide is built in, so just use `-h, --help` option.
Main commands: `clean, convert, merge`. To get help for them pass command without additional args like that: `java -jar JTGMerge.jar clean`.

## Credits

A lot of solutions were taken from [JMerge](https://github.com/Baystation12/JMerge) repository. Big thanks to [@atlantiscze](https://github.com/atlantiscze) for his work.

<hr>

To provide cli-based experience [picocli](https://github.com/remkop/picocli) framework is used.