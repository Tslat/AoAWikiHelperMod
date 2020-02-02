# AoAWikiHelperMod
Helper mod for the Advent of Ascension Wiki

Feel free to contribute via pull-requests to the project. Open to community contributions.

Download a working build here: http://tslat.net/Hosting/Tslat-AoA/AoAWikiHelperMod-1.2.jar

This mod was made to make some automated functions to help with editing and updating the AoA wiki.
The idea being that if we can have this tool automatically print out information for the wiki in an already-formatted layout, it will be much quicker updating individual pages.

Currently supported functionality:
1. Print out all recipes used to create an item.
  How: While ingame, hold the item you want to target in your main hand. Press the appropriate key (defaults to pageup). A text file for that item will be generated in your configs folder for this mod
2. Print out all recipes this item is used in.
    How: while ingame, hold the item you want to target in your main hand. Press the appropriate key (defaults to pagedown). A text file for that item will be generated in your configs folder for this mod.
3. Print out a given loot table.
  How: While ingame or in console, run the command /printloottable <pathtoloottable>. A text file for that loot table will be generated in the configs folder for this mod. Example: /printloottable aoa3:entities/mobs/overworld/charger
    
