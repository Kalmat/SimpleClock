package dev.alef.simpleclock;

import java.util.Arrays;
import java.util.List;

public class Refs {
	
	public static final String MODID = "simpleclock";
	public static final String NAME = "alef's Simple Clock";
	public static final String VERSION = "0.5.5";
	
	public static final int alignUp = 0;
	public static final int alignVCenter = 10;
	public static final int alignDown = 20;
	
	public static final int alignLeft = 0;
	public static final int alignHCenter = 1;
	public static final int alignRight = 2;
	
	public static final int alignUpLeft = alignUp + alignLeft;
    public static final int alignUpCenter = alignUp + alignHCenter;
    public static final int alignUpRight = alignUp + alignRight;
    public static final int alignCenterLeft = alignVCenter + alignLeft;
    public static final int alignCenterRight = alignVCenter + alignRight;
    public static final int alignDownLeft = alignDown + alignLeft;
    public static final int alignDownRight = alignDown + alignRight;
	public static final List<Integer> alignList = Arrays.asList(alignUpLeft,  alignUpCenter, alignUpRight, alignCenterLeft, alignCenterRight, alignDownLeft, alignDownRight);
}
