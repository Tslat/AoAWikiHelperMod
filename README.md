# AoAWikiHelperMod
Helper mod for the Advent of Ascension Wiki

Feel free to contribute via pull-requests to the project. Open to community contributions.

Download a working build here: http://tslat.net/Hosting/Tslat-AoA/AoAWikiHelperMod-1.7.jar

This mod was made to make some automated functions to help with editing and updating the AoA wiki.
The idea being that if we can have this tool automatically print out information for the wiki in an already-formatted layout, it will be much quicker updating individual pages.

Check the list of commands ingame to see what each command does.
Available commands:
- /printarchergunsoverview [clipboard] - Prints out all AoA archerguns data to file. Optionally copy contents to clipboard.
- /printaxesoverview [clipboard] - Prints out all AoA axes data to file. Optionally copy contents to clipboard.
- /printblastersoverview [clipboard] - Prints out all AoA blasters data to file. Optionally copy contents to clipboard.
- /printbowsoverview [clipboard] - Prints out all AoA bows data to file. Optionally copy contents to clipboard.
- /printcannonsoverview [clipboard] - Prints out all AoA cannons data to file. Optionally copy contents to clipboard.
- /printentitiesdroplist Optional:[clipboard] - Prints out the list of entities that drops a given held item. Optionally copy contents to clipboard.
- /printentitydata [clipboard] - Prints out all AoA entity data to file. Optionally copy contents to clipboard.
- /printgreatbladesoverview [clipboard] - Prints out all AoA greatblades data to file. Optionally copy contents to clipboard.
- /printgunsoverview [clipboard] - Prints out all AoA guns data to file. Optionally copy contents to clipboard.
- /printinfusionenchants [clipboard] - Prints out all current imbuing recipes for the Infusion wiki page
- /printrecipes [clipboard] - Prints out all known and supported recipes for the held item. Optionally copy contents to clipboard.
- /printusagerecipes [clipboard] - Prints out all known and supported recipes containing the held item. Optionally copy contents to clipboard.
- /printloottable <Loot Table Path> Optionals:[clipboard|chest OR generic] - Prints out the contents of a given loot table. Optionally copy contents to clipboard. Can also optionally be marked as chest loot or generic (non-entity) loot.
- /printmaulsoverview [clipboard] - Prints out all AoA mauls data to file. Optionally copy contents to clipboard.
- /printpickaxesoverview [clipboard] - Prints out all AoA pickaxes data to file. Optionally copy contents to clipboard.
- /printshotgunsoverview [clipboard] - Prints out all AoA shotguns data to file. Optionally copy contents to clipboard.
- /printshovelsoverview [clipboard] - Prints out all AoA shovels data to file. Optionally copy contents to clipboard.
- /printsnipersoverview [clipboard] - Prints out all AoA snipers data to file. Optionally copy contents to clipboard.
- /printstavesoverview [clipboard] - Prints out all AoA staves data to file. Optionally copy contents to clipboard.
- /printswordsoverview [clipboard] - Prints out all AoA swords data to file. Optionally copy contents to clipboard.
- /printthrownweaponsoverview [clipboard] - Prints out all AoA thrown weapons data to file. Optionally copy contents to clipboard.
- /printtradeoutputs [clipboard] - Prints out all known AoA trader trades resulting in the held item. Optionally copy contents to clipboard.
- /printtradeusages [clipboard] - Prints out all known AoA trader trades using the held item. Optionally copy contents to clipboard.
- /printtradertrades <trader_entity_id> [clipboard] - Prints out all the trades for a given AoA trader. Optionally copy contents to clipboard.
- /printweaponsdata [clipboard] - Prints out all AoA weapons data to file. Optionally copy contents to clipboard.
- /testloottable <Loot Table Path> optionals:[luck:|times:|printtoconsole|pool:] - Prints out a test roll of a given loot table to chat and console. Optionally disable chat portion of printout. Can optionally specify modifiers for luck, number of rolls, or just a specific pool to roll (I.E. luck:5 rolls:20)