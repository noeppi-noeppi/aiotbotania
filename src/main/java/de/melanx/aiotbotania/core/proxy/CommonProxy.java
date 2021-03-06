package de.melanx.aiotbotania.core.proxy;

import com.google.common.collect.ImmutableMap;
import de.melanx.aiotbotania.AIOTBotania;
import de.melanx.aiotbotania.core.Registration;
import de.melanx.aiotbotania.core.config.ConfigHandler;
import de.melanx.aiotbotania.core.network.AIOTBotaniaNetwork;
import de.melanx.aiotbotania.items.terrasteel.RecipeTerraSteelAIOT;
import de.melanx.aiotbotania.items.terrasteel.RecipeTerraSteelAIOTTipped;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.ModTags;

import java.util.Map;

public class CommonProxy implements IProxy {

    public static final ResourceLocation TERRA_RECIPE_ID = new ResourceLocation(AIOTBotania.MODID, "recipe_terrasteel_aiot");
    public static final ResourceLocation TERRA_RECIPE_ID_TIPPED = new ResourceLocation(AIOTBotania.MODID, "recipe_terrasteel_aiot_tipped");
    public static final ResourceLocation TERRA_RECIPE_ID_SHOVEL = new ResourceLocation(AIOTBotania.MODID, "recipe_terrasteel_shovel");
    public static final ResourceLocation TERRA_RECIPE_ID_HOE = new ResourceLocation(AIOTBotania.MODID, "recipe_terrasteel_hoe");

    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::startServer);
    }

    public void setup(FMLCommonSetupEvent event) {
        AIOTBotaniaNetwork.registerPackets();
    }

    public void startServer(FMLServerStartingEvent event) {
        // Insert custom recipes if terra_aiot is enabled.
        if (ConfigHandler.COMMON.TERRA_AIOT.get()) {
            RecipeManager rm = event.getServer().getRecipeManager();
            Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, rm, "field_199522_d");
            @SuppressWarnings({"UnstableApiUsage", "ConstantConditions"})
            Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesNew = recipes.entrySet().stream().map(entry -> {
                if (entry.getKey() == IRecipeType.CRAFTING) {
                    return Pair.of(entry.getKey(), insertRecipe(entry.getValue()));
                } else {
                    return entry;
                }
            }).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
            ObfuscationReflectionHelper.setPrivateValue(RecipeManager.class, rm, recipesNew, "field_199522_d");
        }
    }

    private Map<ResourceLocation, IRecipe<?>> insertRecipe(Map<ResourceLocation, IRecipe<?>> recipeMap) {
        Ingredient terraIngredient = Ingredient.fromTag(ModTags.Items.INGOTS_TERRASTEEL);
        Ingredient twigIngredient = Ingredient.fromItems(ModItems.livingwoodTwig);
        return ImmutableMap.<ResourceLocation, IRecipe<?>>builder().putAll(recipeMap)
                .put(TERRA_RECIPE_ID, new RecipeTerraSteelAIOT(TERRA_RECIPE_ID, "terrasteel_aiot"))
                .put(TERRA_RECIPE_ID_TIPPED, new RecipeTerraSteelAIOTTipped(TERRA_RECIPE_ID_TIPPED, "recipe_terrasteel_aiot_tipped"))
                .put(TERRA_RECIPE_ID_HOE, new ShapedRecipe(TERRA_RECIPE_ID_HOE, "recipe_terrasteel_hoe", 2, 3, NonNullList.from(Ingredient.EMPTY,
                        terraIngredient, terraIngredient,
                        twigIngredient, Ingredient.EMPTY,
                        twigIngredient, Ingredient.EMPTY), new ItemStack(Registration.terrasteel_hoe.get())))
                .put(TERRA_RECIPE_ID_SHOVEL, new ShapedRecipe(TERRA_RECIPE_ID_SHOVEL, "recipe_terrasteel_shovel", 1, 3, NonNullList.from(Ingredient.EMPTY,
                        terraIngredient,
                        twigIngredient,
                        twigIngredient), new ItemStack(Registration.terrasteel_shovel.get())))
                .build();
    }
}
