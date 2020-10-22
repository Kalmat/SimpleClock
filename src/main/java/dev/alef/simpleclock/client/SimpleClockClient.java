package dev.alef.simpleclock.client;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import dev.alef.simpleclock.Refs;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class SimpleClockClient {
	
	private static Minecraft MC = Minecraft.getInstance();
	private static MainWindow MW = MC.getMainWindow();
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
	
    public static void showClock(MatrixStack matrixStack, int alignTo) {
		
		getClockInfo();
        drawClock(matrixStack, alignTo);
    }
	
	private static void getClockInfo() {
		
		double mcTime = (int) MC.world.getDayTime();
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

	private static void drawClock(MatrixStack matrixStack, int alignTo) {
        
    	float scaleA = 1.0F;
		float scaleB = 0.7F;
		float ratio = 1.0F;
		int xGap = 5;
		int yGap = 5;
		String[] text = {TIME, TEXT};

		matrixStack.push();
    	
		matrixStack.scale(scaleA, scaleA, scaleA);
  		ratio = scaleA;
		int x = calcX(alignTo, xGap, text, 0, ratio);
		int y = calcY(alignTo, yGap, text, 0, ratio);
		FR.func_243246_a(matrixStack, new TranslationTextComponent(text[0]), x, y, COLOR);

		matrixStack.scale(scaleB, scaleB, scaleB);
		ratio = (scaleA / scaleB);
		x = calcX(alignTo, xGap, text, 1, ratio);
		y = calcY(alignTo, yGap, text, 1, ratio);
		FR.func_243246_a(matrixStack, new TranslationTextComponent(text[1]), x, y, COLOR);
		
		matrixStack.scale(1.0F, 1.0F, 1.0F);
		matrixStack.pop();
    }
	
	private static int calcX(int alignTo, int xGap, String[] text, int index, float ratio) {
		
		int x = (int) (xGap * ratio);
    	
		if (index >= 0 && index < text.length) {
			
	    	if (alignTo % 10 != Refs.alignLeft) {
	    		
	    		int screenWidth = MW.getScaledWidth();
	    		int textWidth = FR.getStringWidth(text[index]);
	    		
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
		
		if (index >= 0 && index < text.length ) {

			lineHeight = FR.FONT_HEIGHT * index;

			if (alignTo >= Refs.alignVCenter) {
				
	    		int screenHeight = MW.getScaledHeight();
	    		int textHeight = FR.FONT_HEIGHT * text.length;

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
