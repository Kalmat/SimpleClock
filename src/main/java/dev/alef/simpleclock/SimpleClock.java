package dev.alef.simpleclock;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.alef.simpleclock.client.SimpleClockClient;
import dev.alef.simpleclock.config.ConfigFile;
import dev.alef.simpleclock.items.TimeSwordTier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Refs.MODID)
public class SimpleClock {
	
    @SuppressWarnings("unused")
	private final Logger LOGGER = LogManager.getLogger();
    
    private Item TIMESWORD = new SwordItem(TimeSwordTier.time_sword, 0, -2, new Item.Properties().group(ItemGroup.COMBAT));
    private final DeferredRegister<Item> ITEM_REG = DeferredRegister.create(ForgeRegistries.ITEMS, Refs.MODID);
    @SuppressWarnings("unused")
	private final RegistryObject<Item> ITEM = ITEM_REG.register("time_sword", () -> TIMESWORD);
    
	public SimpleClock() {

		// Register other events we use
		final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.register(new onHitEntityListener());
		forgeEventBus.register(new onLooTableLoadListener());

		// Register modloading events
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register items and other stuff
        ITEM_REG.register(modEventBus);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigFile.spec);
        
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> SimpleClockClient.addClientListeners(forgeEventBus, modEventBus));
    }

	public class onHitEntityListener {
		
	    @SubscribeEvent
        public void onHitEntity(final AttackEntityEvent event) {
        	
        	Entity target = event.getTarget();
        	PlayerEntity player = (PlayerEntity) event.getPlayer();
        	
        	Item itemInMainHand = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND).getStack().getItem();
        	
    		if (itemInMainHand.equals(TIMESWORD)) {
                Vector3d look = player.getLookVec().normalize();
                double knockback = TimeSwordTier.getKnockback();
                target.addVelocity(look.getX()*knockback, 0, look.getZ()*knockback);
        	}
		}
	}

	public class onLooTableLoadListener {
		
		@SubscribeEvent
        public void onLootTablesLoad(final LootTableLoadEvent event) {
			
			if (!SimpleClock.isModPresent("coloroutofspace")) {

				// Test: /loot give @p loot minecraft:chests/end_city_treasure
	            if (event.getName().equals(new ResourceLocation("minecraft", "chests/buried_treasure"))
	            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/underwater_ruin_big"))
	            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/pillager_outpost"))
	            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/end_city_treasure"))
	            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/stronghold_library"))
	            	) {
	                event.getTable().addPool(net.minecraft.loot.LootPool.builder()
	                        					.addEntry(TableLootEntry.builder(new ResourceLocation(Refs.MODID, "chests/time_sword")).weight(1))
	                        					.bonusRolls(0, 1)
	                        					.name(Refs.MODID)
	                        					.build()
	                    					);
	            }
			}
		}
	}
            
    public static boolean isModPresent(String modid) {
		for (ModInfo modInfo : FMLLoader.getLoadingModList().getMods()) {
			if (modInfo.getModId().toString().toLowerCase().equals(modid.toLowerCase())) {
				return true;
			}
		}
		return false;
    }
}
