package dev.alef.simpleclock.client;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import dev.alef.simpleclock.Refs;
import dev.alef.simpleclock.config.ConfigFile;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("resource")
public class SimpleClockClient {
	
    @SuppressWarnings("unused")
	private final static Logger LOGGER = LogManager.getLogger();
    
	private static Minecraft MC = Minecraft.getInstance();
	private static MainWindow MW = MC.getMainWindow();
	
	private static int HOURS = 0;
	private static int MINUTES = 0;
	private static String TIME = "";
	private static String TEXT = "";
	private static int LEFT = 0;
	private static int COLOR = 0xffffffff;
	
    private static int ALIGN_INDEX = Refs.alignUpRight;
    private static int ALIGNTO = Refs.alignList.get(ALIGN_INDEX);
	private static int KEYPOS;
	
    public static void addClientListeners(IEventBus forgeEventBus, IEventBus modEventBus) {

 		modEventBus.addListener(SimpleClockClient::clientInit);
    	forgeEventBus.register(new onRenderGameOverlayListener());
        forgeEventBus.register(new onKeyInputListener());
    }
    
	private static void clientInit(final FMLClientSetupEvent event) {
		String key = ConfigFile.GENERAL.Key.get();
		if (key.length() == 0 || key.length() > 1) {
			key = "p";
		}
		SimpleClockClient.KEYPOS = SimpleClockClient.registerKeybindings(key);
		SimpleClockClient.ALIGN_INDEX = ConfigFile.GENERAL.ClockPosition.get();
		if (SimpleClockClient.ALIGN_INDEX >= 0 && SimpleClockClient.ALIGN_INDEX < Refs.alignList.size()) {
			SimpleClockClient.ALIGNTO = Refs.alignList.get(SimpleClockClient.ALIGN_INDEX);
		}
		else {
			SimpleClockClient.ALIGNTO = Refs.alignUpRight;
		}
    }

	public static class onRenderGameOverlayListener {
		
		@SubscribeEvent
		public void OnRenderGameOverlay(final RenderGameOverlayEvent.Text event) {
			SimpleClockClient.showClock(event.getMatrixStack(), SimpleClockClient.ALIGNTO);
		}
	}
	
	public static class onKeyInputListener {
		
		@SubscribeEvent(priority=EventPriority.NORMAL)
		public void onKeyInput(final KeyInputEvent event) {

			if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == SimpleClockClient.KEYPOS && SimpleClockClient.getCurrentScreen() == null) {
				SimpleClockClient.ALIGN_INDEX = (SimpleClockClient.ALIGN_INDEX + 1) % Refs.alignList.size();
				SimpleClockClient.ALIGNTO = Refs.alignList.get(SimpleClockClient.ALIGN_INDEX);
			    ConfigFile.GENERAL.ClockPosition.set(SimpleClockClient.ALIGN_INDEX);
		    }
		}
    }
	
    public static void showClock(MatrixStack matrixStack, int alignTo) {
    	SimpleClockClient.getClockInfo();
    	SimpleClockClient.drawClock(matrixStack, alignTo);
    }
	
	private static void getClockInfo() {
		
		double mcTime = (int) MC.world.getDayTime();
		SimpleClockClient.HOURS = (int) ((mcTime / 1000 + 8) % 24);
		SimpleClockClient.MINUTES = (int) (60 * (mcTime % 1000) / 1000);
		SimpleClockClient.TIME = String.format("%02d:%02d", SimpleClockClient.HOURS, SimpleClockClient.MINUTES);
		SimpleClockClient.COLOR = 0xffffffff;

        if (SimpleClockClient.HOURS >= 8 && SimpleClockClient.HOURS < 21) {
        	SimpleClockClient.LEFT = 21 - SimpleClockClient.HOURS;
        	SimpleClockClient.TEXT = SimpleClockClient.LEFT + "h till Night";
            if (SimpleClockClient.LEFT == 1) {
            	SimpleClockClient.COLOR = 0xffff0000;
            }
        }
    	else {
    		if (SimpleClockClient.HOURS < 8) {
    			SimpleClockClient.LEFT = 8 - SimpleClockClient.HOURS;
    			}
    		else {
    			SimpleClockClient.LEFT = 24 - HOURS + 8;
    		}		
    		SimpleClockClient.TEXT = SimpleClockClient.LEFT + "h till Day";
            if (SimpleClockClient.LEFT == 1) {
            	SimpleClockClient.COLOR = 0xff00ff00;
            }
    	}
	}

	private static void drawClock(MatrixStack matrixStack, int alignTo) {
        
    	float scaleA = 1.0F;
		float scaleB = 0.7F;
		float ratio = 1.0F;
		int xGap = 5;
		int yGap = 5;
		String[] text = {SimpleClockClient.TIME, SimpleClockClient.TEXT};
		FontRenderer fr = Minecraft.getInstance().fontRenderer;

		matrixStack.push();
    	
		matrixStack.scale(scaleA, scaleA, scaleA);
  		ratio = scaleA;
		int x = calcX(alignTo, xGap, text, 0, ratio);
		int y = calcY(alignTo, yGap, text, 0, ratio);
		fr.func_243246_a(matrixStack, new TranslationTextComponent(text[0]), x, y, SimpleClockClient.COLOR);

		matrixStack.scale(scaleB, scaleB, scaleB);
		ratio = (scaleA / scaleB);
		x = calcX(alignTo, xGap, text, 1, ratio);
		y = calcY(alignTo, yGap, text, 1, ratio);
		fr.func_243246_a(matrixStack, new TranslationTextComponent(text[1]), x, y, SimpleClockClient.COLOR);
		
		matrixStack.scale(1.0F, 1.0F, 1.0F);
		matrixStack.pop();
    }
	
	private static int calcX(int alignTo, int xGap, String[] text, int index, float ratio) {
		
		int x = (int) (xGap * ratio);
		FontRenderer fr = Minecraft.getInstance().fontRenderer;
    	
		if (index >= 0 && index < text.length) {
			
	    	if (alignTo % 10 != Refs.alignLeft) {
	    		
	    		int screenWidth = SimpleClockClient.MW.getScaledWidth();
	    		int textWidth = fr.getStringWidth(text[index]);
	    		
	    		if (alignTo % 10 == Refs.alignHCenter) {
	    			x = (int) (((screenWidth * ratio) - textWidth) / 2);
	    		}
	    		else if (alignTo % 10 == Refs.alignRight) {
	    			x = (int) ((screenWidth * ratio) - textWidth - x);
	    		}
	    	}
		}
    	return x;
	}
	
	private static int calcY(int alignTo, int yGap, String[] text, int index, float ratio) {
		
		int y = yGap;
		int lineHeight = 0;
		FontRenderer fr = Minecraft.getInstance().fontRenderer;
		
		if (index >= 0 && index < text.length ) {

			lineHeight = fr.FONT_HEIGHT * index;

			if (alignTo >= Refs.alignVCenter) {
				
	    		int screenHeight = MW.getScaledHeight();
	    		int textHeight = fr.FONT_HEIGHT * text.length;

	    		if (alignTo < Refs.alignDown) {
	    			y = (int) ((screenHeight - textHeight) / 2);
	    		}
	    		else if (alignTo >= Refs.alignDown) {
	    			y = screenHeight - textHeight;
	    		}
			}
		}
		y = (int) ((y + lineHeight) * ratio);
		return y;
	}
                    
	public static Screen getCurrentScreen() {
    	return MC.currentScreen;
    }
    
	private static int registerKeybindings(String stringKey) {
		
		int key = SimpleClockClient.getGLFWCode(stringKey);
	    KeyBinding KEYBIND = new KeyBinding("key.position.desc", key, "key.simpleclock.category");
        ClientRegistry.registerKeyBinding(KEYBIND);
	    return key;
	}

    private static int getGLFWCode(String keyChar) {
    	
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
}
