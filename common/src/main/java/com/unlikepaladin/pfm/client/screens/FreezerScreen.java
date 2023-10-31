package com.unlikepaladin.pfm.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.menus.AbstractFreezerScreenHandler;
import com.unlikepaladin.pfm.menus.FreezerScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FreezerScreen extends HandledScreen<AbstractFreezerScreenHandler> {
    private final Identifier background = new Identifier(PaladinFurnitureMod.MOD_ID,"textures/gui/container/freezer.png");
    private boolean narrow;

    public FreezerScreen(AbstractFreezerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        this.narrow = this.width < 379;
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.narrow) {
            this.renderBackground(context, mouseX, mouseY, delta);
        } else {
            super.render(context, mouseX, mouseY, delta);
        }
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int k;
        int i = this.x;
        int j = this.y;
        context.drawTexture(this.background, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        if (this.handler.isActive()) {
            k = this.handler.getFuelProgress();
            context.drawTexture(this.background, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }
        k = this.handler.getFreezeProgress();
        context.drawTexture(this.background, i + 79, j + 34, 176, 14, k + 1, 16);
    }

}
