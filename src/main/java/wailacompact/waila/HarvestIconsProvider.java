package wailacompact.waila;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.overlay.DisplayUtil;

/**
 * Appends the harvest icons (effective tool + whether the held item can harvest the block) to the block name line, the
 * {@link HarvestIconsRenderer} then draws them in the top right corner of the tooltip.
 */
public class HarvestIconsProvider implements IWailaDataProvider {

    public static final String CONFIG_KEY = "wailacompact.harvesticons";

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        if (currenttip.isEmpty() || !config.getConfig(CONFIG_KEY, true)) return currenttip;

        Block block = accessor.getBlock();
        EntityPlayer player = accessor.getPlayer();
        MovingObjectPosition pos = accessor.getPosition();
        if (block == null || player == null || pos == null) return currenttip;

        int meta = accessor.getMetadata();
        String tool = HarvestHelper.getEffectiveTool(player.worldObj, pos.blockX, pos.blockY, pos.blockZ, block, meta);
        if (tool == null) return currenttip;

        boolean canHarvest = HarvestHelper.isHoldingHarvestTool(player, block, meta, tool);

        String nameLine = currenttip.get(0);
        String icons = HarvestIconsRenderer.create(DisplayUtil.getDisplayWidth(nameLine), tool, canHarvest);
        currenttip.set(0, nameLine + icons);
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

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        return tag;
    }
}
