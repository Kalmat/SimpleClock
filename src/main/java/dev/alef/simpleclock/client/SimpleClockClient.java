package dev.alef.simpleclock.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.world.ClientWorld;

public class SimpleClockClient {
	
	private static final Minecraft MC = Minecraft.getInstance();
	private static final ClientWorld WORLD = MC.world;
	private static final MainWindow MW = MC.mainWindow;
	private static final FontRenderer FR = MC.fontRenderer;
	
	private static int ALIGNTO = 0;
	private static int HOURS = 0;
	private static int MINUTES = 0;
	private static String TIME = "";
	private static String TEXT = "";
	private static int LEFT = 0;
	private static int COLOR = 0xffffffff;
	
    private final static Logger LOGGER = LogManager.getLogger();
	
	public static void showClock(int alignTo) {
		
		ALIGNTO = alignTo;
		
		getClockInfo();
        drawClock();
    }
	
	private static void getClockInfo() {
		
		double mcTime = (int) WORLD.getDayTime();
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

    private static void drawClock() {
        
    	float scaleA = 1.0f;
		float scaleB = 0.7f;
		float ratio = 1.0f;
    	int xGap = 5;
		int yGap = 5;
		
    	GlStateManager.pushMatrix();
    	
		GlStateManager.scaled(scaleA, scaleA, scaleA);
		int fontHeightA = FR.FONT_HEIGHT;
  		ratio = scaleA;
		drawTextUp(TIME, xGap, yGap, COLOR, ratio);
		
		GlStateManager.scaled(scaleB, scaleB, scaleB);
		int fontHeightB = FR.FONT_HEIGHT;
		ratio = (scaleA / scaleB);
		drawTextUp(TEXT, xGap, (int) (fontHeightA + (fontHeightB*1.2)), COLOR, ratio);
		
		GlStateManager.popMatrix();
    }
                    
    private static void drawTextUp(String text, int xGap, int yGap, int color, float ratio) {
    	
    	int x = (int) (xGap * ratio);
    	int y = yGap;
    	
    	if (ALIGNTO != 0) {
    		int screenWidth = MW.getScaledWidth();
    		int textWidth = FR.getStringWidth(text);
    		if (ALIGNTO == 1) {
    			x = (int) (((screenWidth * ratio) - textWidth) / 2);
    			y = yGap;
    			}
    		else if (ALIGNTO == 2) {
    			x = (int) ((screenWidth * ratio) - textWidth - (xGap * ratio));
    			y = yGap;
    		}
    	}
		FR.drawStringWithShadow(text, x, y, color);
	}
}
