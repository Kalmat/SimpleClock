package dev.alef.simpleclock.client;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.alef.simpleclock.Refs;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class SimpleClockClient {
	
	private static Minecraft MC = Minecraft.getInstance();
	private static MainWindow MW = MC.mainWindow;
	private static FontRenderer FR = MC.fontRenderer;
	
	private static int HOURS = 0;
	private static int MINUTES = 0;
	private static String TIME = "";
	private static String TEXT = "";
	private static int LEFT = 0;
	private static int COLOR = 0xffffffff;
	
    @SuppressWarnings("unused")
	private final static Logger LOGGER = LogManager.getLogger();
    
    public SimpleClockClient() {
    }
	
    public static void showClock(int alignTo) {
		
		getClockInfo();
        drawClock(alignTo);
    }
	
	@SuppressWarnings("resource")
	private static void getClockInfo() {
		
		double mcTime = (int) Minecraft.getInstance().world.getDayTime();
        HOURS = (int) ((mcTime / 1000 + 8) % 24);
        MINUTES = (int) (60 * (mcTime % 1000) / 1000);
        TIME = String.format("%02d:%02d", HOURS, MINUTES);
        COLOR = 0xffffffff;

        if (HOURS >= 8 && HOURS < 21) {
        	LEFT = 21 - HOURS;
        	TEXT = LEFT + "h till Night";
            if (LEFT == 1) {
            	COLOR = 0xffff0000;
            }
        }
    	else {
    		if (HOURS < 8) {
    			LEFT = 8 - HOURS;
    			}
    		else {
    			LEFT = 24 - HOURS + 8;
    		}		
    		TEXT = LEFT + "h till Day";
            if (LEFT == 1) {
            	COLOR = 0xff00ff00;
            }
    	}
	}

    private static void drawClock(int alignTo) {
        
    	float scaleA = 1.0f;
		float scaleB = 0.7f;
		float ratio = 1.0f;
    	int xGap = 5;
		int yGap = 5;
		
    	GlStateManager.pushMatrix();
    	
		GlStateManager.scaled(scaleA, scaleA, scaleA);
		int fontHeightA = FR.FONT_HEIGHT;
  		ratio = scaleA;
		drawTextUp(TIME, alignTo, xGap, yGap, COLOR, ratio);
		
		GlStateManager.scaled(scaleB, scaleB, scaleB);
		int fontHeightB = FR.FONT_HEIGHT;
		ratio = (scaleA / scaleB);
		drawTextUp(TEXT, alignTo, xGap, (int) (fontHeightA + (fontHeightB*1.2)), COLOR, ratio);
		
		GlStateManager.popMatrix();
    }
                    
    private static  void drawTextUp(String text, int alignTo, int xGap, int yGap, int color, float ratio) {
    	
    	int x = (int) (xGap * ratio);
    	int y = yGap;
    	
    	if (alignTo != Refs.alignLeft) {
    		int screenWidth = MW.getScaledWidth();
    		int textWidth = FR.getStringWidth(text);
    		if (alignTo == Refs.alignCenter) {
    			x = (int) (((screenWidth * ratio) - textWidth) / 2);
    			y = yGap;
    			}
    		else if (alignTo == Refs.alignRight) {
    			x = (int) ((screenWidth * ratio) - textWidth - (xGap * ratio));
    			y = yGap;
    		}
    	}
		FR.drawStringWithShadow(text, x, y, color);
	}

    @SuppressWarnings("resource")
	public static Screen getCurrentScreen() {
    	return Minecraft.getInstance().currentScreen;
    }
    
	public static int registerKeybindings(String stringKey) {
		
		int key = getGLFWCode(stringKey);
		KeyBinding[] KEYBINDS = new KeyBinding[1];
	    KEYBINDS[0] = new KeyBinding("key.position.desc", key, "key.simpleclock.category");
	    
	    for (int i = 0; i < KEYBINDS.length; ++i) {
	        ClientRegistry.registerKeyBinding(KEYBINDS[i]);
	    }
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
