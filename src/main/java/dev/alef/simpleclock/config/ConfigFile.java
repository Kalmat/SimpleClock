package dev.alef.simpleclock.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class ConfigFile {
	
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Integer> ClockPosition;
        public final ForgeConfigSpec.ConfigValue<String> Key;
        public final ForgeConfigSpec.ConfigValue<Boolean> Debug;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            ClockPosition = builder
                    .comment("Clock Position [0-Left/1-Center/2-Right|default:0]")
                    .define("clockposition", 2);
            Key = builder
                    .comment("Key to change position (avoid reserved keys) [\"(a-z)\"|default:\"p\"]")
                    .define("key", "p");
            Debug = builder
            		.comment("Print always-useless Debug info [false/true|default:false]")
            		.define("debug", false);
            builder.pop();
        }
    }
    
//    @SubscribeEvent
//    public static void onLoad(final ModConfig.Loading configEvent) {
//    }
//
//    @SubscribeEvent
//    public static void onReload(final ConfigReloading configEvent) {
//    }
}