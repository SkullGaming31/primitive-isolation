# Mod Feature Status

This file records implemented features, work in progress, and features tabled for future work. It is a status record, not a general list of ideas.

## Implemented

- **Animal butchering:** Cows, pigs, chickens, mooshrooms, sheep, rabbits, polar bears, hoglins, deer, and moose leave carcasses. Carcasses can be collected or butchered by hand or at the Butchery Table. Sheep and rabbit meat can also be salted and smoked.
- **Animal dung:** Vanilla and modded `Animal` mobs drop one dung item randomly, averaging one drop per 10 minutes for each loaded animal.
- **Aquatic predators:** Piranhas spawn in rivers and swamps; sharks spawn in oceans. Both attack players only while the players are in water. Piranhas drop bones, while sharks drop raw shark meat. Spawn eggs are available for both.
- **Animal reactions:** Nearby animals of the same type flee when an animal is hurt.
- **Flint tools:** Flint tools use the wooden-tool harvest tier and have 29 durability. Flint heads are knapped and assembled at the Knapping and Assembly Station; assemble a tool with its head in the top-center slot, string in the center slot, and a stick in the bottom-center slot.
- **Charcoal pit:** Builds from 3x3 through 9x9, converts logs into charcoal, emits smoke and fire-level light while active, and displays remaining time. Its core lasts a random 1–10 completed runs before burning out and being destroyed. Vanilla log-to-charcoal smelting is disabled.
- **Chunk Anchor:** Keeps only its own chunk loaded. Ender Pearls provide one hour of fuel each by default; the fuel requirement can be disabled in the common config.
- **Structural integrity:** Enabled by default through a common config option. Solid building blocks rely on nearby terrain support and unsupported structures collapse as falling blocks. Timber beams extend horizontal spans, and support posts improve vertical support. Structures are checked after block placement, removal, or neighbor updates; existing builds are not scanned wholesale on world load. Verified in-game by confirming an unsupported dirt bridge collapses.
- **Perishable foods:** Raw meat and fish spoil after 20 minutes, salted foods after 30 minutes, and smoked foods after 150 minutes. Vanilla raw meat and fish loot is replaced with the modded perishable items.
- **Salting Rack:** Processes raw meat and fish one piece at a time, using 2 salt and 1 minute per piece. Food continues to spoil inside; expired inputs are removed and rotten flesh drops beside the rack.
- **Preserving Bin:** Provides 27 storage slots and a dedicated ice slot. One regular Minecraft ice block pauses stored food's spoil timer for 2 hours 20 minutes. When chilling stops, the remaining timer resumes.
- **Spear fishing:** A direct melee hit with a spear kills cod, salmon, tropical fish, or pufferfish while they are in water. Other weapons use normal damage.
- **Squid defense and food:** Squids target players in water, fire damaging ink projectiles on a 30-second cooldown, and can pull nearby shoreline players into the water. When hurt underwater, their ink burst blinds nearby underwater players for 10 seconds. Squids drop raw squid, a perishable food item.
- **Ice Shards:** Regular ice mined without Silk Touch drops 2–4 shards. Eight shards around a snow block craft regular ice; Silk Touch still drops ice.
- **Campfires and primitive fire starting:** Rain extinguishes exposed campfires, which can be relit with flint and steel. The Hand Drill lights campfires and starts charcoal pits after 5 seconds of continuous use, consuming durability on success. Lit campfires can start normal fire beside flammable wood blocks within 2 blocks in any direction, averaging about once per minute per block.
- **Bark:** Stripping a log with an axe drops 1 bark.
- **Hide and tannery:** Field butchering a cow yields 1 raw hide; Butchery Table processing yields 2. Mooshrooms yield 0–2 raw hide when butchered. Leather drops from horses, donkeys, mules, llamas, and trader llamas are replaced with the same count of raw hide. Pigs yield pork only. Use a flint knife in the offhand to scrape raw hide into cleaned hide, consuming knife durability. Craft cleaned hide with bark to make tanned hide. The Drying Rack accepts one tanned hide at a time, saves progress, and turns it into leather. Clear outdoor drying takes 10 minutes; indoor drying takes 20 minutes. Exposed rain pauses drying, a lit campfire within 3 blocks doubles the rate, and the rack shows wet, partially dry, nearly dry, and ready stages.
- **Woven basket:** Craft from 4 sugar cane and 3 sticks. A placed basket has 7 slots for food and gathered materials, including plants, sticks, and stones. Filled contents persist when broken and are restored when placed; a carried basket cannot be opened. Crafted and tested in game.
- **Grass fibers, climbable rope, and sleeping mat:** Harvest short or tall grass with a flint knife (1 durability per grass block) for a 1-in-8 chance to get a fiber. Craft a climbable rope segment from 3 fibers vertically; place it against a sturdy block or another rope segment, then extend it downward. Craft a sleeping mat from 3 wool blocks over 3 hay bales. Sleeping on the mat advances time at 10× overnight without setting a respawn point or fading the screen to black; vanilla beds remain limited to night.

## In Progress

- **Known bug — daytime sleeping mat use (#4):** Using the mat during daytime wakes the player immediately instead of advancing time. Woven floor mats are deferred.
- **Butchery Table 3D carcass display:** The cow model renders black on the table. The lighting fix is being tested on cows first before applying it to the other carcass types.
- **Gathering wild plants (#6):** The first roster is implemented in code: biome-generated wild barley, turnips, berries, yarrow, and bitterleaf. Wild barley harvests edible grain and may also drop separate replantable seeds; turnips require a digging stick. The field guide records identified entries, raw bitterleaf causes temporary poison, and yarrow treats it. A separate plant dryer prepares yarrow and bitterleaf. The full build and startup data/registry loading succeed; hands-on gameplay testing is still required before completion.

## Todo

- **Decorative woven floor mats (#5):** Add craftable, placeable decorative woven floor mats. The recipe, materials, placement form, and appearance need to be decided before implementation. This is separate from the sleeping mat and does not include sleeping or respawn behavior.

## Tabled for future work
