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
 * outputs straight into a chest.
 */
public class GTRecipeNBTProvider implements IWailaDataProvider {

    public static final String TAG_CRAFTING_ITEM = "wailacompact.craftingItem";
    public static final String TAG_CRAFTING_FLUID = "wailacompact.craftingFluid";
    public static final String TAG_AMOUNT = "wailacompact.amount";

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        Object mte = getMetaTileEntity(te);
        if (mte == null) return tag;

        Object items = getPublicField(mte, "mOutputItems");
        if (items instanceof ItemStack[] stacks) {
            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] == null) continue;
                NBTTagCompound itemTag = new NBTTagCompound();
                stacks[i].writeToNBT(itemTag);
                // writeToNBT stores the stack size as a byte
                itemTag.setInteger(TAG_AMOUNT, stacks[i].stackSize);
                tag.setTag(TAG_CRAFTING_ITEM + i, itemTag);
            }
        }

        Object fluid = getPublicField(mte, "mOutputFluid");
        if (fluid instanceof FluidStack fluidStack) {
            tag.setTag(TAG_CRAFTING_FLUID, fluidStack.writeToNBT(new NBTTagCompound()));
        }
        return tag;
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
