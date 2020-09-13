package dev.alef.simpleclock;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("simpleclock")
public class SimpleClock {
	
	public static final String MODID = "simpleclock";
	public static final String NAME = "alef's Simple Clock";
	public static final String VERSION = "0.5.5";
	
    private final Logger LOGGER = LogManager.getLogger();
    
    private static PlayerEntity PLAYER;
    private Item TIMESWORD = new SwordItem(TimeSwordTier.time_sword, 0, -2, new Item.Properties().group(ItemGroup.COMBAT));
    private final DeferredRegister<Item> CONTAINER = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    private final RegistryObject<Item> ITEM = CONTAINER.register("time_sword", () -> TIMESWORD);
	
    private String[] ALIGNLIST = {"Left", "Center", "Right"};
    private int ALIGNTO = ConfigFile.GENERAL.ClockPosition.get();
	
	private int KEYPOS = GLFW.GLFW_KEY_P;
	private boolean debug = false;

	public SimpleClock() {

		// Register modloading events
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        
        // Register other events we use
        MinecraftForge.EVENT_BUS.register(new onPlayerJoinListener());
        MinecraftForge.EVENT_BUS.register(new onRenderGameOverlayListener());
        MinecraftForge.EVENT_BUS.register(new onHitEntityListener());
        MinecraftForge.EVENT_BUS.register(new onLooTableLoadListener());
        MinecraftForge.EVENT_BUS.register(new onKeyInputListener());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register items and other stuff
        CONTAINER.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        // Load config file
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ConfigFile.spec);
        KEYPOS = getGLFWCode(ConfigFile.GENERAL.Key.get());;
	    debug = ConfigFile.GENERAL.Debug.get();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
    	if (debug) {
    		LOGGER.info("HELLO from PREINIT");
    	}
    }
    
	@SuppressWarnings("resource")
	private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    	if (debug) {
    		LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    	}
    	RegisterKeybinding();
    }
	
	private void RegisterKeybinding() {
		
    	if (debug) {
    		LOGGER.info("HELLO while Registering Keys");
    	}
	    KeyBinding[] KEYBINDS = new KeyBinding[1];
	    KEYBINDS[0] = new KeyBinding("key.position.desc", KEYPOS, "key.simpleclock.category");
	    for (int i = 0; i < KEYBINDS.length; ++i) {
	        ClientRegistry.registerKeyBinding(KEYBINDS[i]);
	    }
	}

    private int getGLFWCode(String keyChar) {
    	
    	int keyCode = GLFW.GLFW_KEY_P; // default key
    	
    	if (keyChar.length() > 1) {
    		keyChar = keyChar.substring(1,2);
    	}
    	
    	List<String> protectedKeys = Arrays.asList("q", "w", "e", "t", "a", "s", "d", "f", "l");
    	if (!protectedKeys.contains(keyChar)) {
	    	for (int i = 65; i <= 90; ++i) {
				if (keyChar.equalsIgnoreCase(GLFW.glfwGetKeyName(i, GLFW.glfwGetKeyScancode(i)))) {
	    			keyCode = i;
	    			break;
	    		}
	    	}
    	}
    	return keyCode;
    }
    
    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(MODID, "helloworld", () -> { 
        	LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    
    public class onPlayerJoinListener {
    
		@SubscribeEvent
        public void onPlayerJoin(final EntityJoinWorldEvent event) {
            if (event.getEntity() != null && 
                event.getEntity() instanceof PlayerEntity) {
            	if (debug) {
            		LOGGER.info("WELCOME " + event.getEntity().getName().getFormattedText() + "!!!");
            	}
            	PLAYER = (PlayerEntity) event.getEntity();
            }
        }
    }

	public class onRenderGameOverlayListener {
	
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public void OnRenderGameOverlay(final RenderGameOverlayEvent.Text event) {
			
	    	if (debug) {
	    		LOGGER.info("Render CLOCK");
	    	}
			SimpleClockClient.showClock(ALIGNTO);
		}
			
	}

	public class onHitEntityListener {
		
	    @SubscribeEvent
        public void onHitEntity(AttackEntityEvent event) {
        	
        	Entity target = event.getTarget();
        	PlayerEntity player = (PlayerEntity) event.getPlayer();
        	
        	// if (target != null && player != null&& player.equals(SimpleClock.PLAYER)) { // --> NECESSARY? TEST IT!!!!
        		
        	ItemStack stackInMainHand = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
        	
    		if (stackInMainHand.getStack().getItem().equals(TIMESWORD)) {
    	    	if (debug) {
    	    		LOGGER.info("Knockback!!! from "+player.getName().getFormattedText()+" to "+target.getName().getFormattedText()
    	    					+"Using Items: "+stackInMainHand.getStack().getItem().getName()+TIMESWORD.getName());
    	    	}
                Vec3d look = player.getLookVec().normalize();
                double knockback = TimeSwordTier.getKnockback();
                target.addVelocity(look.getX()*knockback, (look.getY()+2)*knockback, look.getZ()*knockback);
    		}
		}
	}

	public class onLooTableLoadListener {
		
		@SubscribeEvent
        public void onLootTablesLoad(final LootTableLoadEvent event) {
	    	if (debug) {
	    		LOGGER.info("HELLO from adding entries to chests loot tables");
	    	}

			// Test: /loot give @p loot minecraft:chests/end_city_treasure
            if (event.getName().equals(new ResourceLocation("minecraft", "chests/buried_treasure"))
            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/underwater_ruin_big"))
            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/pillager_outpost"))
            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/shipwreck_treasure"))
            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/end_city_treasure"))
            	|| event.getName().equals(new ResourceLocation("minecraft", "chests/stronghold_library"))
            	) {          	
            	event.getTable().addPool(LootPool.builder().addEntry(TableLootEntry.builder(new ResourceLocation(MODID, "chests/time_sword"))).build());
            }
        }
	}
	
	public class onKeyInputListener {
		
		@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
		@OnlyIn(Dist.CLIENT)
		public void onKeyInput(final KeyInputEvent event) {

	    	if (debug) {
	    		LOGGER.info("KEY Pressed");
	    	}

			if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEYPOS) {
		    	ALIGNTO++;
		    	if (ALIGNTO >= ALIGNLIST.length) {
		    		ALIGNTO = 0;
		    	}
			    ConfigFile.GENERAL.ClockPosition.set(ALIGNTO);
		    	if (debug) {
		    		LOGGER.info("P Pressed. Changing Position");
		    	}
		    }
		}
    }
}
