package wailacompact.waila;

import java.awt.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaVariableWidthTooltipRenderer;
import mcp.mobius.waila.api.SpecialChars;

/**
 * Draws a small tool icon with a green check mark (the held item can harvest the block) or a red cross in its bottom
 * right corner, aligned to the right edge of the tooltip.
 * <p>
 * Params: [0] width of the text before this renderer on its line, [1] tool class, [2] "1" if harvestable
 */
public class HarvestIconsRenderer implements IWailaVariableWidthTooltipRenderer {

    public static final String NAME = "wailacompact.harvest";

    private static final String CHECK = "✔";
    private static final String CROSS = "✘";
    private static final int CHECK_COLOR = 0x55FF55;
    private static final int CROSS_COLOR = 0xFF5555;

    private static final int GAP = 6; // Minimum space between the block name and the icons
    private static final int ICON_SIZE = 10;
    private static final float ICON_SCALE = ICON_SIZE / 16.0F;
    private static final float MARK_SCALE = 0.6F;
    private static final int MARK_OVERHANG = 2; // How far the mark sticks out of the icon to the right and bottom

    private static final RenderItem renderItem = new RenderItem();

    private int maxLineWidth;

    public static String create(int prefixWidth, String toolClass, boolean canHarvest) {
        return SpecialChars.getRenderString(NAME, String.valueOf(prefixWidth), toolClass, canHarvest ? "1" : "0");
    }

    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        return new Dimension(GAP + getIconsWidth(), 8);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        if (params.length < 3) return;
        int prefixWidth = parseInt(params[0]);
        String toolClass = params[1];
        boolean canHarvest = "1".equals(params[2]);

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;

        // The GL matrix is at the position of this renderer, right after the block name. Move to the right edge.
        int x = Math.max(GAP, maxLineWidth - prefixWidth - getIconsWidth());

        ItemStack icon = ToolIcons.get(toolClass);
        if (icon != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(x, -1, 0);
            GL11.glScalef(ICON_SCALE, ICON_SCALE, 1.0F);
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            try {
                renderItem.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), icon, 0, 0);
            } catch (Exception ignored) {}
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        } else {
            // Unknown tool class without an icon, show its first letter instead
            String letter = toolClass.isEmpty() ? "?" : toolClass.substring(0, 1).toUpperCase();
            font.drawStringWithShadow(letter, x + (ICON_SIZE - font.getStringWidth(letter)) / 2, 0, 0xFFFFFF);
        }

        // Small mark over the bottom right corner of the icon
        String mark = canHarvest ? CHECK : CROSS;
        float markX = x + ICON_SIZE + MARK_OVERHANG - font.getStringWidth(mark) * MARK_SCALE;
        float markY = -1 + ICON_SIZE + MARK_OVERHANG - font.FONT_HEIGHT * MARK_SCALE;
        GL11.glPushMatrix();
        GL11.glTranslatef(markX, markY, 0);
        GL11.glScalef(MARK_SCALE, MARK_SCALE, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        font.drawStringWithShadow(mark, 0, 0, canHarvest ? CHECK_COLOR : CROSS_COLOR);
        GL11.glPopMatrix();
    }

    @Override
    public void setMaxLineWidth(int width) {
        maxLineWidth = width;
    }

    @Override
    public int getMaxLineWidth() {
        return maxLineWidth;
    }

    private static int getIconsWidth() {
        return ICON_SIZE + MARK_OVERHANG;
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
