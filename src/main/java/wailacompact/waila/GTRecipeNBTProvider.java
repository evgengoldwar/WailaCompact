package wailacompact.waila;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

/**
 * Server side: sends what a GregTech single block machine is currently crafting (mOutputItems / mOutputFluid of
 * MTEBasicMachine), so the compact recipe line can show it even when the output slots are empty because the machine
 * outputs straight into a chest. For multiblocks, sends the inputs of the running recipe.
 */
public class GTRecipeNBTProvider implements IWailaDataProvider {

    public static final String TAG_CRAFTING_ITEM = "wailacompact.craftingItem";
    public static final String TAG_CRAFTING_FLUID = "wailacompact.craftingFluid";
    public static final String TAG_AMOUNT = "wailacompact.amount";
    public static final String TAG_RECIPE_INPUT_ITEM = "wailacompact.recipeInputItem";
    public static final String TAG_RECIPE_INPUT_FLUID = "wailacompact.recipeInputFluid";

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        Object mte = getMetaTileEntity(te);
        if (mte == null) return tag;

        Object items = getPublicField(mte, "mOutputItems");
        if (items instanceof ItemStack[]stacks) {
            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] == null) continue;
                NBTTagCompound itemTag = new NBTTagCompound();
                stacks[i].writeToNBT(itemTag);
                // writeToNBT stores the stack size as a byte
                itemTag.setLong(TAG_AMOUNT, stacks[i].stackSize);
                tag.setTag(TAG_CRAFTING_ITEM + i, itemTag);
            }
        }

        Object fluid = getPublicField(mte, "mOutputFluid");
        if (fluid instanceof FluidStack fluidStack) {
            tag.setTag(TAG_CRAFTING_FLUID, fluidStack.writeToNBT(new NBTTagCompound()));
        }

        writeMultiBlockRecipeInputs(te, mte, tag);
        return tag;
    }

    /**
     * Multiblocks consume the inputs when a recipe starts, the recipe they run is kept in their ProcessingLogic
     * (lastRecipe, times getCurrentParallels()).
     */
    private static void writeMultiBlockRecipeInputs(TileEntity te, Object mte, NBTTagCompound tag) {
        if (!isActive(te)) return;
        Object logic = getDeclaredField(mte, "processingLogic");
        if (logic == null) return;
        Object recipe = getDeclaredField(logic, "lastRecipe");
        if (recipe == null) return;

        long parallels = 1;
        try {
            Object result = logic.getClass()
                .getMethod("getCurrentParallels")
                .invoke(logic);
            if (result instanceof Integer p && p > 0) parallels = p;
        } catch (ReflectiveOperationException | RuntimeException ignored) {}

        Object items = getPublicField(recipe, "mInputs");
        if (items instanceof ItemStack[]stacks) {
            int index = 0;
            for (ItemStack stack : stacks) {
                if (stack == null || stack.getItem() == null) continue;
                NBTTagCompound itemTag = new NBTTagCompound();
                stack.writeToNBT(itemTag);
                // Not consumed inputs (programmed circuits...) have a stack size of 0
                long amount = stack.stackSize <= 0 ? 1 : stack.stackSize * parallels;
                itemTag.setLong(TAG_AMOUNT, amount);
                tag.setTag(TAG_RECIPE_INPUT_ITEM + index++, itemTag);
            }
        }

        Object fluids = getPublicField(recipe, "mFluidInputs");
        if (fluids instanceof FluidStack[]fluidStacks) {
            int index = 0;
            for (FluidStack fluidStack : fluidStacks) {
                if (fluidStack == null || fluidStack.getFluid() == null) continue;
                NBTTagCompound fluidTag = fluidStack.writeToNBT(new NBTTagCompound());
                fluidTag.setLong(TAG_AMOUNT, fluidStack.amount * parallels);
                tag.setTag(TAG_RECIPE_INPUT_FLUID + index++, fluidTag);
            }
        }
    }

    private static boolean isActive(TileEntity te) {
        try {
            return Boolean.TRUE.equals(
                te.getClass()
                    .getMethod("isActive")
                    .invoke(te));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    /**
     * Value of a (possibly protected) field declared in the class of the object or one of its superclasses.
     */
    private static Object getDeclaredField(Object owner, String name) {
        for (Class<?> c = owner.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(owner);
            } catch (NoSuchFieldException e) {
                // Look in the superclass
            } catch (ReflectiveOperationException | RuntimeException e) {
                return null;
            }
        }
        return null;
    }

    private static Object getMetaTileEntity(TileEntity te) {
        try {
            Method getMetaTileEntity = te.getClass()
                .getMethod("getMetaTileEntity");
            return getMetaTileEntity.invoke(te);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Object getPublicField(Object owner, String name) {
        try {
            Field field = owner.getClass()
                .getField(name);
            return field.get(owner);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }
}
