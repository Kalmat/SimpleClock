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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import dev.alef.simpleclock.client.SimpleClockClient;
import dev.alef.simpleclock.config.ConfigFile;
import dev.alef.simpleclock.items.TimeSwordTier;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Refs.MODID)
public class SimpleClock {
	
    private final Logger LOGGER = LogManager.getLogger();
    
    private Item TIMESWORD = new SwordItem(TimeSwordTier.time_sword, 0, -2, new Item.Properties().group(ItemGroup.COMBAT));
    private final DeferredRegister<Item> ITEM_REG = DeferredRegister.create(ForgeRegistries.ITEMS, Refs.MODID);
    @SuppressWarnings("unused")
	private final RegistryObject<Item> ITEM = ITEM_REG.register("time_sword", () -> TIMESWORD);
	
    private int ALIGN_INDEX = Refs.alignUpRight;
    private int ALIGNTO = Refs.alignList[ALIGN_INDEX];
	
	private int KEYPOS;
	@SuppressWarnings("unused")
	private boolean debug = false;
	
	public SimpleClock() {

		// Register modloading events
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        
        // Register other events we use
        MinecraftForge.EVENT_BUS.register(new onRenderGameOverlayListener());
        MinecraftForge.EVENT_BUS.register(new onHitEntityListener());
        MinecraftForge.EVENT_BUS.register(new onLooTableLoadListener());
        MinecraftForge.EVENT_BUS.register(new onKeyInputListener());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register items and other stuff
        ITEM_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        // Load config file
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigFile.spec);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
    }
    
	private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    	KEYPOS = SimpleClockClient.registerKeybindings(ConfigFile.GENERAL.Key.get());
        ALIGN_INDEX = ConfigFile.GENERAL.ClockPosition.get();
        ALIGNTO = Refs.alignList[ALIGN_INDEX];
        debug = ConfigFile.GENERAL.Debug.get();
    }
	
    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(Refs.MODID, "helloworld", () -> { 
        	LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    
	public class onRenderGameOverlayListener {
	
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void OnRenderGameOverlay(final RenderGameOverlayEvent.Text event) {
			SimpleClockClient.showClock(event.getMatrixStack(), ALIGNTO);
		}
	}

	public class onHitEntityListener {
		
	    @SubscribeEvent
        public void onHitEntity(AttackEntityEvent event) {
        	
        	Entity target = event.getTarget();
        	PlayerEntity player = (PlayerEntity) event.getPlayer();
        	
        	Item itemInMainHand = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND).getStack().getItem();
        	
    		if (itemInMainHand.equals(TIMESWORD)) {
                Vector3d look = player.getLookVec().normalize();
                double knockback = TimeSwordTier.getKnockback();
                target.addVelocity(look.getX()*knockback, (look.getY()+2)*knockback, look.getZ()*knockback);
        	}
		}
	}

	public class onLooTableLoadListener {
		
		@SubscribeEvent
        public void onLootTablesLoad(final LootTableLoadEvent event) {

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
            
	public class onKeyInputListener {
		
		@SubscribeEvent(priority=EventPriority.NORMAL)
		@OnlyIn(Dist.CLIENT)
		public void onKeyInput(final KeyInputEvent event) {

			if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEYPOS && SimpleClockClient.getCurrentScreen() == null) {
				ALIGN_INDEX = (ALIGN_INDEX + 1) % Refs.alignList.length;
				ALIGNTO = Refs.alignList[ALIGN_INDEX];
			    ConfigFile.GENERAL.ClockPosition.set(ALIGN_INDEX);
		    }
		}
    }
}
