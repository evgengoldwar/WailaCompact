package wailacompact.waila;

import java.lang.reflect.Field;

import cpw.mods.fml.common.Loader;

/**
 * Turns off the durability / charge bars other mods draw on items while a tool icon is rendered:
 * <ul>
 * <li>GregTech draws them inside its tool renderer (GTMod.proxy.mRenderItemDurabilityBar / mRenderItemChargeBar)</li>
 * <li>DuraDisplay draws them from RenderItem.renderItemOverlayIntoGUI (DurabilityRenderer.Execute)</li>
 * </ul>
 */
public class DurabilityBars {

    private static final Toggle[] TOGGLES = {
        new Toggle("gregtech", "gregtech.GTMod", "proxy", "mRenderItemDurabilityBar"),
        new Toggle("gregtech", "gregtech.GTMod", "proxy", "mRenderItemChargeBar"),
        new Toggle("duradisplay", "com.caedis.duradisplay.render.DurabilityRenderer", null, "Execute") };

    /**
     * @return the previous values to pass to {@link #restore}
     */
    public static boolean[] hide() {
        boolean[] previous = new boolean[TOGGLES.length];
        for (int i = 0; i < TOGGLES.length; i++) previous[i] = TOGGLES[i].set(false);
        return previous;
    }

    public static void restore(boolean[] previous) {
        for (int i = 0; i < TOGGLES.length; i++) TOGGLES[i].set(previous[i]);
    }

    private static class Toggle {

        private final String modId;
        private final String className;
        private final String instanceField; // Static field holding the object with the flag, null for a static flag
        private final String flagField;

        private boolean resolved;
        private Object owner;
        private Field flag;

        Toggle(String modId, String className, String instanceField, String flagField) {
            this.modId = modId;
            this.className = className;
            this.instanceField = instanceField;
            this.flagField = flagField;
        }

        /**
         * @return the previous value, or the new one if the flag doesn't exist
         */
        boolean set(boolean value) {
            if (!resolve()) return value;
            try {
                boolean previous = flag.getBoolean(owner);
                flag.setBoolean(owner, value);
                return previous;
            } catch (ReflectiveOperationException | RuntimeException e) {
                flag = null;
                return value;
            }
        }

        private boolean resolve() {
            if (!resolved) {
                resolved = true;
                if (!Loader.isModLoaded(modId)) return false;
                try {
                    Class<?> clazz = Class.forName(className);
                    if (instanceField != null) {
                        owner = clazz.getField(instanceField)
                            .get(null);
                        if (owner == null) return false;
                        flag = owner.getClass()
                            .getField(flagField);
                    } else {
                        flag = clazz.getField(flagField);
                    }
                } catch (ReflectiveOperationException | RuntimeException e) {
                    flag = null;
                }
            }
            return flag != null;
        }
    }
}
