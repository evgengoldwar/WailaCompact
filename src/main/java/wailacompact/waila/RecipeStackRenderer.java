package wailacompact.waila;

import java.awt.Dimension;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.registry.GameData;
import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.api.SpecialChars;

/**
 * One element of the compact recipe line: an item or fluid icon with a short amount (64, 1.5k, 1M...) in its bottom
 * right corner, or a vertically centered text (arrow, "+3").
 * <p>
 * Params: [0] kind ("i" item, "f" fluid, "t" text), then
 * <ul>
 * <li>item: [1] registry name, [2] meta, [3] amount</li>
 * <li>fluid: [1] fluid name, [2] amount in L</li>
 * <li>text: [1] text, [2] color</li>
 * </ul>
 */
public class RecipeStackRenderer implements IWailaTooltipRenderer {

    public static final String NAME = "wailacompact.stack";

    private static final int SIZE = 16;
    private static final int SPACING = 2;
    private static final float AMOUNT_SCALE = 0.5F;

    private static final RenderItem renderItem = new RenderItem();

    public static String item(ItemStack stack, long amount) {
        String name = GameData.getItemRegistry()
            .getNameForObject(stack.getItem());
        if (name == null) return "";
        return SpecialChars
            .getRenderString(NAME, "i", name, String.valueOf(stack.getItemDamage()), String.valueOf(amount));
    }

    public static String fluid(Fluid fluid, long amount) {
        return SpecialChars.getRenderString(NAME, "f", fluid.getName(), String.valueOf(amount));
    }

    public static String text(String text, int color) {
        return SpecialChars.getRenderString(NAME, "t", text, String.valueOf(color));
    }

    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        if (params.length > 1 && "t".equals(params[0])) {
            return new Dimension(Minecraft.getMinecraft().fontRenderer.getStringWidth(params[1]) + SPACING, SIZE);
        }
        return new Dimension(SIZE + SPACING, SIZE);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        if (params.length < 2) return;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        switch (params[0]) {
            case "i" -> {
                if (params.length < 4) return;
                Item item = (Item) Item.itemRegistry.getObject(params[1]);
                if (item == null) return;
                drawItem(new ItemStack(item, 1, parseInt(params[2])));
                drawAmount(font, formatAmount(parseLong(params[3])));
            }
            case "f" -> {
                if (params.length < 3) return;
                Fluid fluid = FluidRegistry.getFluid(params[1]);
                if (fluid == null) return;
                drawFluid(fluid);
                drawAmount(font, formatAmount(parseLong(params[2])));
            }
            case "t" -> {
                int color = params.length > 2 ? parseInt(params[2]) : 0xFFFFFF;
                font.drawStringWithShadow(params[1], 0, (SIZE - 8) / 2, color);
            }
            default -> {}
        }
    }

    private static void drawItem(ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        boolean[] bars = DurabilityBars.hide();
        try {
            renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, 0, 0);
        } catch (Exception ignored) {} finally {
            DurabilityBars.restore(bars);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }

    private static void drawFluid(Fluid fluid) {
        IIcon icon = fluid.getIcon();
        if (icon == null) return;
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);
        int color = fluid.getColor();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, SIZE, 0, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(SIZE, SIZE, 0, icon.getMaxU(), icon.getMaxV());
        tessellator.addVertexWithUV(SIZE, 0, 0, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(0, 0, 0, icon.getMinU(), icon.getMinV());
        tessellator.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawAmount(FontRenderer font, String amount) {
        if (amount.isEmpty()) return;
        GL11.glPushMatrix();
        GL11.glTranslatef(
            SIZE + 1 - font.getStringWidth(amount) * AMOUNT_SCALE,
            SIZE + 1 - font.FONT_HEIGHT * AMOUNT_SCALE,
            0);
        GL11.glScalef(AMOUNT_SCALE, AMOUNT_SCALE, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        font.drawStringWithShadow(amount, 0, 0, 0xFFFFFF);
        GL11.glPopMatrix();
    }

    /**
     * 1 -> "" (like in the inventory), 64 -> "64", 1500 -> "1.5k", 1000000 -> "1M"
     */
    public static String formatAmount(long amount) {
        if (amount <= 1) return "";
        if (amount < 1000) return String.valueOf(amount);
        String[] suffixes = { "k", "M", "G", "T", "P", "E" };
        double value = amount;
        int i = -1;
        while (value >= 1000 && i < suffixes.length - 1) {
            value /= 1000;
            i++;
        }
        String number = value >= 100 ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
        if (number.endsWith(".0")) number = number.substring(0, number.length() - 2);
        return number + suffixes[i];
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
