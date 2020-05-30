package WayofTime.alchemicalWizardry.client.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import codechicken.nei.ItemList;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.StatCollector;
import WayofTime.alchemicalWizardry.api.items.ShapedBloodOrbRecipe;
import WayofTime.alchemicalWizardry.api.items.interfaces.IBloodOrb;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;

import static WayofTime.alchemicalWizardry.client.nei.NEIConfig.bloodOrbs;

/**
 * NEI Blood Orb Shaped Recipe Handler by joshie *
 */
public class NEIBloodOrbShapedHandler extends ShapedRecipeHandler {
	public class CachedBloodOrbRecipe extends CachedShapedRecipe {
		public CachedBloodOrbRecipe(int width, int height, Object[] items, ItemStack out) {
			super(width, height, items, out);
		}

		@Override
		public void setIngredients(int width, int height, Object[] items) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (items[y * width + x] == null)
						continue;

					Object o = items[y * width + x];
					if (o instanceof ItemStack) {
						PositionedStack stack = new PositionedStack(items[y * width + x], 25 + x * 18, 6 + y * 18, false);
						stack.setMaxSize(1);
						ingredients.add(stack);
					} else if (o instanceof Integer) {
						ArrayList<ItemStack> orbs = new ArrayList();
						for (Item item : bloodOrbs) {
							if (((IBloodOrb) item).getOrbLevel() >= (Integer) o) {
								orbs.add(new ItemStack(item));
							}
						}
						PositionedStack stack = new PositionedStack(orbs, 25 + x * 18, 6 + y * 18, false);
						stack.setMaxSize(1);
						ingredients.add(stack);
					} else if(o instanceof List) {
						PositionedStack stack = new PositionedStack(o, 25 + x * 18, 6 + y * 18, false);
						stack.setMaxSize(1);
						ingredients.add(stack);
					}
				}
			}
		}
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if (outputId.equals("crafting") && getClass() == NEIBloodOrbShapedHandler.class) {
			for (IRecipe irecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList()) {
				if (irecipe instanceof ShapedBloodOrbRecipe) {
					CachedBloodOrbRecipe recipe = forgeShapedRecipe((ShapedBloodOrbRecipe) irecipe);
					if (recipe == null)
						continue;

					recipe.computeVisuals();
					arecipes.add(recipe);
				}
			}
		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {
		for (IRecipe irecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList()) {
			if (irecipe instanceof ShapedBloodOrbRecipe) {
				CachedBloodOrbRecipe recipe = forgeShapedRecipe((ShapedBloodOrbRecipe) irecipe);
				if (recipe == null || !NEIServerUtils.areStacksSameTypeCrafting(recipe.result.item, result))
					continue;

				recipe.computeVisuals();
				arecipes.add(recipe);
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		for (IRecipe irecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList()) {
			CachedShapedRecipe recipe = null;
			if (irecipe instanceof ShapedBloodOrbRecipe)
				recipe = forgeShapedRecipe((ShapedBloodOrbRecipe) irecipe);

			if (recipe == null || !recipe.contains(recipe.ingredients, ingredient.getItem()))
				continue;

			recipe.computeVisuals();
			if (recipe.contains(recipe.ingredients, ingredient)) {
				recipe.setIngredientPermutation(recipe.ingredients, ingredient);
				arecipes.add(recipe);
			}
		}
	}

	private CachedBloodOrbRecipe forgeShapedRecipe(ShapedBloodOrbRecipe recipe) {
		int width;
		int height;
		try {
			width = recipe.width;
			height = recipe.height;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Object[] items = recipe.getInput();
		for (Object item : items)
			if (item instanceof List && ((List<?>) item).isEmpty())// ore
																	// handler,
																	// no ores
				return null;

		return new CachedBloodOrbRecipe(width, height, items, recipe.getRecipeOutput());
	}
	
	@Override
    public void loadTransferRects() {
        transferRects.add(new RecipeTransferRect(new Rectangle(84, 23, 24, 18), "crafting"));
    }


	@Override
	public String getRecipeName() {
		return StatCollector.translateToLocal("bm.string.crafting.orb.shaped");

	}
    @Override
    public TemplateRecipeHandler newInstance() {
        // We need to populate available blood orbs,
        //   but don't try to do it more than once
	    if (bloodOrbs.isEmpty()) {
            for (ItemStack item : ItemList.items) {
                if (item != null && item.getItem() instanceof IBloodOrb) {
                    bloodOrbs.add(item.getItem());
                }
            }
            if (bloodOrbs.isEmpty()) {
                // If there is NEI no cache - go to item registry
                for (Object anItemRegistry : Item.itemRegistry) {
                    Item item = (Item) anItemRegistry;
                    if (item != null && item instanceof IBloodOrb) {
                        bloodOrbs.add(item);
                    }
                }
            }
        }
        return super.newInstance();
	}

}