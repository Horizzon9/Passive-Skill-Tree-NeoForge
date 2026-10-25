# PORTING NOTES — Passive Skill Tree 1.20.1 Forge → 1.21.1 NeoForge

Unofficial port. Original mod by Daripher (GPL): https://github.com/Daripher/Passive-Skill-Tree
Source reference: original v0.6.14a sources (exact match for `PassiveSkillTree-1.20.1-BETA-0.6.14a-all.jar`).
Target: Minecraft 1.21.1, NeoForge 21.1.x, Java 21, ModDevGradle, Parchment 1.21.1-2024.11.17.

## Build
- ModDevGradle 2.0.148 (`net.neoforged.moddev`), Gradle 8.12.1, toolchain Java 21.
- Mixins: `[[mixins]] config = "skilltree.mixins.json"` in `neoforge.mods.toml`. No refmap
  (NeoForge runs Mojang mappings at runtime). MixinExtras comes bundled with NeoForge 21.1.
- `pack.mcmeta` → `pack_format: 34`.

## Dependency status (1.21.1 NeoForge)
| Mod | Status | Notes |
|---|---|---|
| curios (9.5.1+1.21.1) | required, ported | SlotAttribute/ICurioItem/SlotResult APIs changed |
| attributefix (21.1.3) | required, no code | declared only; needs Bookshelf + Prickle at runtime (dev `runtimeOnly` from the Modrinth maven) |
| itemproductionlib | **inlined** | no 1.21.1 build. Only `Interactive`, `ItemProducedEvent` and the vanilla producer hooks are used: classes copied into `daripher.itemproduction` (original by Daripher, github.com/Daripher/Item-Production-Lib) and the vanilla mixins (block entity user, furnace, brewing stand, campfire, crafting table, smithing) ported under `daripher.skilltree.mixin.itemproduction`. The Farmer's Delight / Dave's Potioneering / Visual Workbench hooks are not ported. |
| apotheosis (8.9.0) | **disabled** | 1.21.1 Apotheosis moved `adventure.*` → `apotheosis.socket/affix/loot` and changed gem/affix data formats + GemBonus API; the mod's own gem/socket system takes over (`apotheosisEnabled()` = false). `ApotheosisCompatibility` is a stub. 5 apotheosis mixins removed. |
| attributeslib → apothic_attributes (2.11.0) | ported | package rename `dev.shadowsoffire.attributeslib.client` → `dev.shadowsoffire.apothic_attributes.client`; ModifierSource API unchanged. Mod id is `apothic_attributes` |
| tetra | **disabled** | no 1.21.1 NeoForge build exists; `TetraCompatibility` is a stub |
| jei (19.x) | ported | API unchanged |
| easymagic (21.1.4) | ported | see the Mixins table |

## Core API migrations
- Packages: `net.minecraftforge.*` → `net.neoforged.neoforge.*`; `net.minecraftforge.fml.*` → `net.neoforged.fml.*`;
  `net.minecraftforge.eventbus.api` → `net.neoforged.bus.api`; `net.minecraftforge.api.distmarker` → `net.neoforged.api.distmarker`.
- `@Mod.EventBusSubscriber(bus=...)` → `net.neoforged.fml.common.EventBusSubscriber` (the `bus` attribute is
  ignored in FML 4: IModBusEvent handlers go to the mod bus, the rest to the game bus automatically).
- `@Mod` constructor receives `IEventBus` + `ModContainer`; `ModLoadingContext.registerConfig` → `ModContainer.registerConfig`.
- `MinecraftForge.EVENT_BUS` → `NeoForge.EVENT_BUS`; `ForgeConfigSpec` → `ModConfigSpec`.
- `ResourceLocation` ctor → `ResourceLocation.fromNamespaceAndPath(ns, path)` / `ResourceLocation.parse(str)`.
- `RegistryObject<T>` → `DeferredHolder<T, T>`; `ForgeRegistries.X` → `BuiltInRegistries.X` (singular names:
  `ITEM`, `BLOCK`, `ENTITY_TYPE`, `ATTRIBUTE`, `MOB_EFFECT`, `RECIPE_SERIALIZER`, `LOOT_POOL_ENTRY_TYPE`).
- Custom registries (`PSTRegistries`): `DeferredRegister.makeRegistry` is gone; each registry is created in
  `NewRegistryEvent` with `new RegistryBuilder<>(ResourceKey.createRegistryKey(id))` and looked up through
  `BuiltInRegistries.REGISTRY` (kept as `Supplier<Registry<T>>` so call sites still use `.get()`).
- Capabilities → data attachments: `IPlayerSkills`/`PlayerSkills` registered as `AttachmentType` in `PSTAttachments`
  with `AttachmentType.serializable(PlayerSkills::new).copyOnDeath().build()`; access via `player.getData(...)` /
  `player.hasData(...)`. `AttachCapabilitiesEvent`/`PlayerEvent.Clone` handling removed (copyOnDeath covers it).
  `INBTSerializable` methods now take `HolderLookup.Provider`.
- Networking: `SimpleChannel` → `CustomPacketPayload` records with `StreamCodec`, registered in
  `RegisterPayloadHandlersEvent` via `PayloadRegistrar.playToClient/playToServer`; send with
  `PacketDistributor.sendToPlayer(player, payload)` / `sendToServer(payload)`. Client-only handling lives in
  `client.ClientMessageHandler`.

## Attribute / effect API
- `AttributeModifier` is a record `(ResourceLocation id, double amount, Operation operation)`.
  `Operation`: `ADDITION→ADD_VALUE`, `MULTIPLY_BASE→ADD_MULTIPLIED_BASE`, `MULTIPLY_TOTAL→ADD_MULTIPLIED_TOTAL`;
  `toValue()/fromValue(v)` → `id()` / `Operation.BY_ID.apply(id)`.
- Attributes are `Holder<Attribute>` everywhere (registries, `player.getAttribute(Holder)`, `getAttributeValue`).
  `Attribute instanceof SlotAttribute` → `attribute.value() instanceof SlotAttribute`.
- `UUID` modifier ids → `ResourceLocation`. Data-format compatible: JSON/NBT `"id"` still accepts and writes plain
  UUID strings (mapped to `skilltree:<uuid>`); `SerializationHelper.deserializeModifierId/serializeModifierId`.
- Mob effects are `Holder<MobEffect>`; `MobEffectInstance` ctor takes `Holder<MobEffect>`.
- Curios slot growth: `CuriosApi.getSlotHelper().growSlotType(...)` is gone →
  `CuriosApi.getCuriosInventory(p).ifPresent(inv -> inv.getStacksHandler(id).ifPresent(h -> h.grow(n)))` (and `shrink`).

## Item stack NBT → data components (`PSTComponents`)
| Old NBT | Component |
|---|---|
| `SkillBonuses` list | `skilltree:skill_bonuses` (`List<CompoundTag>`, bonus serialization unchanged) |
| `DurabilityBonuses` list | `skilltree:durability_bonuses` |
| `gem_bonuses` list | `skilltree:gem_bonuses` |
| `gems` (gem type ids) | `skilltree:gems` (`List<ResourceLocation>`) |
| gem item `type` | `skilltree:gem_type` (`ResourceLocation`) |
| `Poisons` list | `skilltree:poisons` (`List<MobEffectInstance>`) |
| `Arrows` stack + `ArrowsCount` | `skilltree:arrows` + `skilltree:arrows_count` |
| `MixedPotion` flag | `skilltree:mixed_potion` |
| `CustomPotionColor` | vanilla `minecraft:potion_contents` (`PotionContents.customColor`) |

## Enchantments / potions / recipes
- `EnchantmentCategory` removed. `EnchantmentCondition.met(ItemStack, Holder<Enchantment>)` now tests the item
  being enchanted (`EquipmentCondition.isArmor/isWeapon/...`). JSON format unchanged.
- `PotionUtils` removed → `DataComponents.POTION_CONTENTS` (`PotionContents`); `PotionHelper` rewritten.
- Recipes: `Recipe` now needs `MapCodec` + `StreamCodec`; `matches`/`assemble` take `RecipeInput`
  (`CraftingInput` for crafting). `RecipeSerializer.createCodec/serializer(...)` replaces `fromJson`/`fromNetwork`.
- Loot: `LootPoolEntryType` built from `MapCodec`; `LootPoolSingletonContainer.Serializer` → codec-based.

## Events (renames)
- `LivingAttackEvent` / `LivingHurtEvent` → `LivingIncomingDamageEvent` (cancellable, `getAmount/setAmount`).
- `TickEvent.PlayerTickEvent` → `net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre` (`getEntity()`).
- `TickEvent.ClientTickEvent` → `net.neoforged.neoforge.client.event.ClientTickEvent.Pre/Post`.
- `EntityItemPickupEvent` → `ItemEntityPickupEvent.Pre` (`setCanPickup(TriState)`).
- `ShieldBlockEvent` via `ForgeHooks.onShieldBlock` → removed; blocking logic applied directly.
- `RegisterGuiOverlaysEvent`/`IGuiOverlay`/`ForgeGui` → `RegisterGuiLayersEvent` + `net.minecraft.client.gui.LayeredDraw.Layer`.
- `ForgeEventFactory.onPlayerDestroyItem` → `EventHooks.onPlayerDestroyItem`; `ToolActions` → `ItemAbilities`.
- `Item.appendHoverText(stack, Item.TooltipContext, List, TooltipFlag)`; `ItemStack.isSameItemSameTags` → `isSameItemSameComponents`.
- `IForgeItem` → `net.neoforged.neoforge.common.extensions.IItemExtension` (`canApplyAtEnchantingTable` →
  `supportsEnchantment`/`isPrimaryItemFor`).

## Data folders (1.21 renames)
- `data/<ns>/recipes` → `recipe`; `loot_tables` → `loot_table`; `advancements` → `advancement`;
  `tags/items` → `tags/item`. Recipe results use `"id"` instead of `"item"`.
- `data/forge/tags/items/...` → `data/c/tags/item/...` (convention tags).
- Curios 1.21.1 (9.x): slot defs `data/<ns>/curios/slots/<id>.json` (fields `size`, `icon`, `order`,
  `validators`), entity assignment `data/<ns>/curios/entities/<id>.json` (`entities`, `slots`).
  Slot item tags stay `curios:<slot>` under `data/curios/tags/item/`.

## Mixins (1.21.1 status)
| Mixin | Status |
|---|---|
| `EnchantmentMenuMixin` | targets `lambda$slotsChanged$0` / `lambda$clickMenuButton$1`, `EventHooks.onEnchantmentLevelSet`, `getEnchantmentList(RegistryAccess, ...)` |
| `PotionItemMixin` | `PotionUtils` is gone: `@ModifyExpressionValue` on the `POTION_CONTENTS` lookup in `finishUsingItem` / `appendHoverText` applies the duration/amplifier bonuses |
| `PotionUtilsMixin` | replaced by `ThrownPotionMixin` (splash and lingering potions) |
| `InventoryMenuMixin` | superclass is now `RecipeBookMenu<CraftingInput, CraftingRecipe>`; `EventHooks.firePlayerCraftingEvent` |
| `ItemMixin` | food bonuses rebuild the `FoodProperties` record (nutrition and saturation scaled) |
| `LivingEntityMixin` | `dropAllDeathLoot(ServerLevel, DamageSource)`; jump bonus only on `getJumpPower(F)F` (the no-arg overload delegates to it) |
| `AnvilMenuMixin` | `getAllEnchantments()` → `EnchantmentHelper.getEnchantmentsForCrafting`; `require = 0` removed |
| `ArmorSlotMixin` | armor slots are the package-private `net.minecraft.world.inventory.ArmorSlot` since 1.20.5 |
| `RecipeBookComponentMixin` (client) | `RecipeCollection.craftable` holds `RecipeHolder`s |
| `CraftingMenuMixin`, `ResultSlotMixin` (new) | set the crafting player for skill-gated recipes (1.21 `CraftingInput` has no container or player) |
| `EditBoxAccessor` | `frame` field removed → `focusedTime` (cursor blink) |
| `PlayerMixin`, `SmithingMenuMixin`, `SimpleContainerMixin`, `AbstractContainerMenuMixin`, `CraftingContainerMixin`, `AbstractArrowMixin`, `FishingHookMixin`, `ItemStackMixin`, other accessors | targets unchanged |
| Apotheosis mixins (5) | removed, compat disabled |
| `easymagic/ModEnchantmentMenuMixin` | EasyMagic 21.1 merged `updateLevels`/`createEnchantmentInstance` into `updateLevelsAndClues`; the mixin redirects `getEnchantmentCost` and `getEnchantmentList` there. It is applied only when EasyMagic is loaded (`PSTMixinPlugin`). |

All mixins were checked with a forced `MixinEnvironment.audit()` at server start. None failed.

## Other runtime fixes
- Skill-gated recipes: 1.21 recipes no longer know their id, so `SkillRequiringRecipe` resolves it through the recipe
  manager (cached). The player comes from a thread-local set in `CraftingMenu.slotChangedCraftingGrid` and `ResultSlot.onTake`.
- Gson `Component.Serializer` is private in 1.21 → `ComponentJsonSerializer` (via `ComponentSerialization.CODEC`).
- `PlayerInteractEvent` is abstract → one listener per cancellable subclass.
- `ItemAttributeModifierEvent` slot: `ItemStack#getEquipmentSlot` is null for non-armor items; it now falls back to `Equipable` or `MAINHAND`.
- All `toUpperCase()`/`toLowerCase()` calls use `Locale.ROOT` (on a Turkish system locale enum parsing broke: `SHIELD` → `SHİELD`).
- Forge tags in data and item conditions migrate automatically: `forge:x` → `c:x` (`tools/bows→tools/bow`,
  `tools/crossbows→tools/crossbow`, `tools/tridents→tools/spear`, `tools/knives→tools/knife`).
- Custom registries are created statically with `RegistryBuilder.create()` and registered in `NewRegistryEvent`.
- `neoforge.mods.toml` lives in `src/main/templates` (ModDevGradle `generateModMetadata` task).
- Deprecated `bus = Bus.MOD` removed from all `@EventBusSubscriber`s (FML 4 routes `IModBusEvent`s to the mod bus).
- Resources missing from the source tree were restored from the 1.20.1 jar in the 1.21 layout: 588 skills, skill tree,
  31 gem types, recipes (`recipe/`, result `item`→`id`), advancements (`advancement/`), `loot_table/gems.json`,
  `tags/item`, `c` convention tags, Curios item tags, item models and the `en_us` / `ru_ru` lang files.

## Verification done
- `./gradlew build` → `build/libs/PassiveSkillTree-1.21.1-NeoForge-0.6.14a.jar`.
- `runServer` starts with no errors. The datapack loads 588 skills, 1 tree, 31 gem types, 50 recipes, 21 advancements and the gem loot table.
- `runClient` in singleplayer, with a scripted test that was removed afterwards: all 588 skills learned and removed on a player
  without errors; attribute bonuses apply and revert; XP → skill points → learning a skill through the real network payloads;
  client sync; data attachment round-trip; skills kept after death and respawn; Curios ring (2), necklace and quiver slots present;
  the skill tree screen, the editor screen, item models, gem models and tooltips render.
- Multiplayer: a client connected to the dedicated server and stayed connected.

## Known issues / disabled compat
- **Apotheosis compat disabled**: Apotheosis 1.21.1 (8.x) moved `adventure.*` to `apotheosis.socket/affix/loot` and changed the
  gem/affix data formats and the GemBonus API. The mod's own gem/socket system is used instead. `data/skilltree/affixes`,
  `data/skilltree/gems` and `assets/apotheosis/models/item/gems` ship but are inert; the `apotheosis_gems` loot table is not shipped.
- **Tetra compat disabled**: there is no NeoForge 1.21.1 Tetra.
- **ItemProductionLib third-party hooks** (Farmer's Delight, Dave's Potioneering, Visual Workbench) are not ported, so items made
  there get no crafted-item skill bonuses.
- The shield-block skill no longer fires `LivingShieldBlockEvent` (NeoForge 21.1 has no public hook for it). The block
  outcome (cancelled damage, shield damage, shield-block animation) is applied directly.
- Curios slot-size growth uses `ICurioStacksHandler.grow/shrink` instead of the removed `SlotHelper`.
- When EasyMagic is absent, Mixin logs a harmless WARN (`@Mixin target ... ModEnchantmentMenu was not found`); the mixin is skipped.
- Not exercised in-game: editing in the skill tree editor, gem insertion at the smithing table, quiver filling, potion mixing,
  weapon poisoning, JEI and Apothic Attributes integration.