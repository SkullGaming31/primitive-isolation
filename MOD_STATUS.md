# Mod Feature Status

This file records features implemented in the mod and features that have been discussed and tabled for future work. It is a status record, not a general list of ideas.

## Implemented

- **Animal butchering:** Cows, pigs, and chickens leave carcasses. Carcasses can be collected or butchered by hand or at the Butchery Table.
- **Animal reactions:** Nearby animals of the same type flee when an animal is hurt.
- **Flint tools:** Flint tools use the wooden-tool harvest tier and have 29 durability. Flint heads are knapped and assembled at the Knapping and Assembly Station.
- **Charcoal pit:** Builds from 3x3 through 9x9, converts logs into charcoal, emits smoke and fire-level light while active, and displays remaining time. Its core lasts a random 1–10 completed runs before burning out and being destroyed. Vanilla log-to-charcoal smelting is disabled.
- **Chunk Anchor:** Keeps only its own chunk loaded. Ender Pearls provide one hour of fuel each by default; the fuel requirement can be disabled in the common config.
- **Perishable foods:** Raw meat and fish spoil after 20 minutes, salted foods after 30 minutes, and smoked foods after 150 minutes. Vanilla raw meat and fish loot is replaced with the modded perishable items.
- **Salting Rack:** Processes raw meat and fish one piece at a time, using 2 salt and 1 minute per piece. Food continues to spoil inside; expired inputs are removed and rotten flesh drops beside the rack.
- **Preserving Bin:** Provides 27 storage slots and a dedicated ice slot. One regular Minecraft ice block pauses stored food's spoil timer for 2 hours 20 minutes. When chilling stops, the remaining timer resumes.
- **Spear fishing:** A direct melee hit with a spear kills cod, salmon, tropical fish, or pufferfish while they are in water. Other weapons use normal damage.
- **Ice Shards:** Regular ice mined without Silk Touch drops 2–4 shards. Eight shards around a snow block craft regular ice; Silk Touch still drops ice.
- **Campfire fire risk:** A lit campfire can start normal fire beside directly adjacent flammable wood blocks, averaging about once per minute per block.

## Tabled for future work

### Cow Hide and Tannery

This feature has been discussed but is not implemented. It is scoped to cows:

- Field butchering with a flint knife yields 1 raw hide; Butchery Table processing yields 2.
- Scraping converts one raw hide into cleaned hide using a flint knife in the offhand, consuming knife durability.
- Stripping logs yields bark; tanning uses 1 bark per cleaned hide to produce tanned hide.
- A drying rack processes one tanned hide at a time, with individual saved progress. Clear outdoor drying takes 10 minutes; indoor drying is slower, and rain slows or pauses drying only when the rack is exposed.
- A campfire within 3 blocks speeds drying. Visual stages show wet, partially dried, nearly dry, and ready-to-collect hide.
