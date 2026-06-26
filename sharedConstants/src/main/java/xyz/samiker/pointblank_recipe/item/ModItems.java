package xyz.samiker.pointblank_recipe.item;

import net.minecraft.item.Item;
import xyz.samiker.pointblank_recipe.utils.ItemRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    // --- RIFLE ---
    public static final Item FRAME_OF_RIFLE_MAGAZINE = register("frameoftheriflemagazine");
    public static final Item RIFLE_MAGAZINE = register("riflemagazine");
    public static final Item FRAME_OF_RIFLE = register("frameoftherifle");
    public static final Item RIFLE_BUTTSTOCK = register("theriflebutt");
    public static final Item BARREL_OF_RIFLE = register("thebarreloftherifle");
    public static final Item IRON_BUTTSTOCK = register("ironbutt");
    public static final Item THE_BARREL_OF_THE_RIFLE_UP = register("thebarreloftherifleup");
    public static final Item FRAME_M4_TYPE = register("framem4a1type");
    public static final Item BUTT_FOR_M4_TYPE = register("buttform4a1type");
    public static final Item HANDLE_RIFLE = register("handlerifle");
    public static final Item SILENCER_FOR_RIFLE = register("silencerforrifle");
    public static final Item SCOPE_FOR_RIFLE = register("scopeforrifle");

    // --- MACHINEGUN ---
    public static final Item FRAME_MK_TYPE = register("framemktype");
    public static final Item MAGAZINE_FOR_MACHINEGUN = register("magazineformachinegun");

    // --- OTHER ---
    public static final Item COPPER_NUGGET = register("coppernugget");
    public static final Item BASIS_FOR_THE_ROCKET = register("basisfortherocket");

    // --- PISTOLS ---
    public static final Item GUN_MAGAZINE = register("gunmagazine");
    public static final Item GUN_POINT = register("gunpoint");
    public static final Item THE_FRAME_OF_THE_GUN = register("theframeofthegun");
    public static final Item FRAME_GUN_MAGAZINE = register("framegunmagazine");

    // --- RPG & MINIGUNS ---
    public static final Item MUZZLE_OF_MINIGUN = register("muzzleofminigun");
    public static final Item FRAME_OF_MINIGUN = register("frameofminigun");
    public static final Item FRAME_OF_AT4 = register("frameofat4");
    public static final Item TRIGGER_MECHANISM = register("triggermechanism");
    public static final Item AUTOGUIDANCE_MODULE = register("autoguidancemodule");
    public static final Item FRAME_OF_M32_MGL = register("frameofm32mgl");

    // --- SHOTGUN ---
    public static final Item FRAME_OF_SHOTGUN = register("frameofshotgun");
    public static final Item BUTT_OF_SHOTGUN = register("buttofshotggun");
    public static final Item SHOTGUN_RELOADER = register("shotgunreloader");
    public static final Item SHOTGUN_BARREL = register("shotgunbarrel");

    // --- SNIPER RIFLES ---
    public static final Item FRAME_OF_SNIPER_RIFLE = register("frameofsniperifle");
    public static final Item SNIPE_SCOPE = register("snipescope");
    public static final Item MUZZLE_OF_SNIPER_RIFLE = register("muzzleofsniperrifle");

    // --- SUBMACHINE GUNS ---
    public static final Item FRAME_OF_SUBMACHINE_GUN = register("frameofsubmachinegun");
    public static final Item BUTT_OF_SUBMACHINE_GUN = register("buttofsubmachinegun");
    public static final Item MAGAZINE_OF_SUBMACHINE_GUN = register("magazineofsubmachinegun");
    public static final Item HANDLE_OF_SUBMACHINE_GUN = register("handleofsubmachinegun");
    public static final Item BUTT_UMP = register("buttump");
    public static final Item FRAME_P90 = register("framep90");
    public static final Item BUTT_FOR_VECTOR = register("buttforvector");

    // --- WEAPON PACKS ---
    public static final Item ENERGY_CUBE = register("energycube");
    public static final Item FRAME_CYBERPUNK_GUN = register("framecyberpunkgun");
    public static final Item FRAME_CYBERPUNK_ENERGY_GUN = register("framecyberpunkenergygun");
    public static final Item COLLIMATOR_SCOPE = register("collimatorscope");
    public static final Item M79 = register("m79");
    public static final Item SNIPER_RIFLE_BILLET_TRAUMA = register("sniperriflebillettrauma");
    public static final Item SNIPER_RIFLE_LOADING_MECHANISM = register("sniperrifleloadingmechanism");
    public static final Item RIFLE_FLAME_ARRESTER = register("rifleflamearrester");
    public static final Item FRAME_RPG = register("framerpg");
    public static final Item SHOTGUN_RELOADER_BRUM = register("shotgunreloaderbrum");
    public static final Item FRAME_OF_OBREZ = register("frameofobrez");

    // --- METRO 2033 ---
    public static final Item FRAME_OF_2012 = register("frameof2012");
    public static final Item FRAME_OF_ABZATS = register("frameofabzats");
    public static final Item FRAME_OF_ASHOT = register("frameofashot");
    public static final Item FRAME_OF_BIGUN = register("frameofbigun");
    public static final Item HORIZONTAL_GUN_MAGAZINE = register("horizontalgunmagazine");
    public static final Item FRAME_OF_HELSING = register("frameofhelsing");
    public static final Item FRAME_OF_MEDVED = register("frameofmedved");
    public static final Item FRAME_OF_PREVED = register("frameofpreved");
    public static final Item FRAME_OF_TIKAR = register("frameoftikar");
    public static final Item FRAME_OF_VSV = register("frameofvsv");
    public static final Item FRAME_OF_UBOINIK = register("frameofuboinik");

    private static Item register(String id) {
        Item item = ItemRegistry.simple(id);
        ITEMS.put(id, item);
        return item;
    }

    public static void registerAll() {
    }

    public static Collection<Item> getItems() {
        return ITEMS.values();
    }
}