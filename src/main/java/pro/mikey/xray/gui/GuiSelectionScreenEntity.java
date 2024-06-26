package pro.mikey.xray.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.XRay;
import pro.mikey.xray.gui.manage.EntityGuiEdit;
import pro.mikey.xray.gui.manage.GuiAddBlock;
import pro.mikey.xray.gui.manage.GuiBlockList;
import pro.mikey.xray.gui.manage.GuiEdit;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.gui.utils.ScrollingList;
import pro.mikey.xray.gui.utils.SupportButton;
import pro.mikey.xray.keybinding.KeyBindings;
import pro.mikey.xray.store.BlockStore;
import pro.mikey.xray.store.EntityStore;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.utils.EntityData;
import pro.mikey.xray.xray.Controller;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSelectionScreenEntity extends GuiBase {
    private static final ResourceLocation CIRCLE = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    private Button distButtons;
    private EditBox search;
    public ItemRenderer render;

    private String lastSearch = "";

    private ArrayList<EntityData> entityList, originalList;
    private ScrollingEntityList scrollList;

    public GuiSelectionScreenEntity() {


        super(true);
        this.setSideTitle(I18n.get("xray.single.tools"));
        ////////////////////////////////
//        EntityStore entityStore = new EntityStore();
//        entityStore.populateGameEntities();
//        System.out.println(0);
//        entityStore.write();
//        System.out.println(1);
//        entityStore.setStorewithSimple(entityStore.read());;
//        System.out.println(entityStore.getStore().isEmpty());
//        System.out.println(2);
//        entityStore.toggleDrawing(EntityType.ALLAY);
//        System.out.println(3);
//        entityStore.write();
//        System.out.println(4);
        /////////////////////////////////

        // Inject this hear as everything is loaded
        if (ClientController.entityStore.getStore().isEmpty()) {
//            List<BlockData.SerializableBlockData> blocks = ClientController.blockStore.populateDefault();
//            Controller.getBlockStore().setStore(BlockStore.getFromSimpleBlockList(blocks));
//            ClientController.blockStore.created = false;

            ClientController.entityStore.setStorewithSimple(ClientController.entityStore.read());
            ClientController.entityStore.write();
        }


        this.entityList = new ArrayList<>(ClientController.entityStore.getStore().values());
        this.entityList.sort(Comparator.comparing(EntityData::isDrawing).reversed());

        this.originalList = this.entityList;
    }

    @Override
    public void init() {
        if (getMinecraft().player == null)
            return;
        this.render = this.itemRenderer;
        this.children().clear();

        this.scrollList = new ScrollingEntityList((getWidth() / 2) - 37, getHeight() / 2 + 10, 203, 185, this.entityList, this);
        addRenderableWidget(this.scrollList);

        this.search = new EditBox(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);


//
//        addRenderableWidget(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 16, 120, 20, I18n.get("xray.input.add_look"), "xray.tooltips.add_block_looking_at", button -> {
//            Player player = getMinecraft().player;
//            if (getMinecraft().level == null || player == null)
//                return;
//
//            this.onClose();
//            try {
//                Vec3 look = player.getLookAngle();
//                Vec3 start = new Vec3(player.blockPosition().getX(), player.blockPosition().getY() + player.getEyeHeight(), player.blockPosition().getZ());
//                Vec3 end = new Vec3(player.blockPosition().getX() + look.x * 100, player.blockPosition().getY() + player.getEyeHeight() + look.y * 100, player.blockPosition().getZ() + look.z * 100);
//
//                ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
//                BlockHitResult result = getMinecraft().level.clip(context);
//
//                if (result.getType() == HitResult.Type.BLOCK) {
//                    Block lookingAt = getMinecraft().level.getBlockState(result.getBlockPos()).getBlock();
//
//                    player.closeContainer();
//                    getMinecraft().setScreen(new GuiAddBlock(lookingAt, GuiSelectionScreenEntity::new));
//                } else
//                    player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.nothing_infront")), false);
//            } catch (NullPointerException ex) {
//                player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.thats_odd")), false);
//            }
//        }));
//
//        addRenderableWidget(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 6, 120, 20, I18n.get("xray.input.show-lava", Controller.isLavaActive()), "xray.tooltips.show_lava", button -> {
//            Controller.toggleLava();
//            button.setMessage(Component.translatable("xray.input.show-lava", Controller.isLavaActive()));
//        }));
        // side bar buttons

        addRenderableWidget(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 60, 120, 20, I18n.get("entity.xray.toggle"), "xray.tooltips.add_block_in_hand", button -> {
//            if (Controller.isEntityxrayActive() == true){
//                Controller.setAutoAimActive(false);
//                Controller.setEntityxrayActive(!Controller.isEntityxrayActive());
//            }
//            else{
//                Controller.setEntityxrayActive(!Controller.isEntityxrayActive());
//            }
            Controller.setEntityxrayActive(!Controller.isEntityxrayActive());

        }));
        ///
        addRenderableWidget(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 38, 120, 20, I18n.get("entity.xray.AutoAimtoggle"), "xray.tooltips.add_block_in_hand", button -> {
//            if (Controller.isAutoAimActive() == false){
//                Controller.setEntityxrayActive(true);
//                Controller.setAutoAimActive(!Controller.isAutoAimActive());
//            }
//            else{
//                Controller.setAutoAimActive(!Controller.isAutoAimActive());
//            }
            Controller.setAutoAimActive(!Controller.isAutoAimActive());


        }));
        addRenderableWidget(new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 - 16, 120, 20, I18n.get("entity.xray.outline",Configuration.store.EntityOutlineMode.get()), "xray.tooltips.add_block", button -> {
            Controller.switchOutlineMode();
            button.setMessage(Component.translatable("entity.xray.outline",Configuration.store.EntityOutlineMode.get()));
        }));



        addRenderableWidget(new Button(getWidth() / 2 + 79, getHeight() / 2 + 58, 60, 20, Component.translatable("entity.xray.GuiMOre"), button -> {
            getMinecraft().player.closeContainer();
            getMinecraft().setScreen(new AutoAimGuiMore());
        }));

        addRenderableWidget(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 36, 120, 20, I18n.get("xray.input.distance", Configuration.store.EntityRadius.get()*3), "xray.tooltips.distance", button -> {
            Controller.incrementEntityCurrentDist();
            button.setMessage(Component.translatable("xray.input.distance", Configuration.store.EntityRadius.get()*3));
        }));


        addRenderableWidget(new Button((getWidth() / 2 + 79) + 62, getHeight() / 2 + 58, 59, 20, Component.translatable("xray.single.close"), button -> {
            this.onClose();
        }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!search.isFocused() && keyCode == KeyBindings.toggleEntityXRayGui.getKey().getValue()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateSearch() {
        if (lastSearch.equals(search.getValue()))
            return;

        if (search.getValue().equals("")) {
            this.entityList = this.originalList;
            this.scrollList.updateEntries(this.entityList);
            lastSearch = "";
            return;
        }

        this.entityList = this.originalList.stream()
                .filter(b -> b.getEntityType().toShortString().toLowerCase().contains(search.getValue().toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.entityList.sort(Comparator.comparing(EntityData::isDrawing));

        this.scrollList.updateEntries(this.entityList);
        lastSearch = search.getValue();
    }

    @Override
    public void tick() {
        super.tick();

        if (search != null) {
            search.tick();
        }

        updateSearch();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (search.mouseClicked(x, y, mouse))
            this.setFocused(search);

        if (mouse == 1 && distButtons.isMouseOver(x, y)) {
            Controller.decrementEntityCurrentDist();
            distButtons.setMessage(Component.translatable("xray.input.distance", Controller.getEntityVisualRadius()));
            distButtons.playDownSound(Minecraft.getInstance().getSoundManager());
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        this.search.render(stack, x, y, partialTicks);
        this.scrollList.render(stack, x, y, partialTicks);

        if (!search.isFocused() && search.getValue().equals(""))
            Minecraft.getInstance().font.drawShadow(stack, I18n.get("xray.single.search"), (float) getWidth() / 2 - 130, (float) getHeight() / 2 - 101, Color.GRAY.getRGB());
    }

    @Override
    public void removed() {
        Configuration.store.EntityRadius.save();
        Configuration.store.EntityOutlineMode.save();
        //ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
        ClientController.entityStore.write();
        //Controller.requestBlockFinder(true);

        super.removed();
    }

    static final class SupportButtonInner extends SupportButton {
        public SupportButtonInner(int widthIn, int heightIn, int width, int height, String text, String i18nKey, OnPress onPress) {
            super(widthIn, heightIn, width, height, Component.literal(text), Component.translatable(i18nKey), onPress);
        }
    }

    static class ScrollingEntityList extends ScrollingList<ScrollingEntityList.EntitySlot> {
        static final int SLOT_HEIGHT = 35;
        public GuiSelectionScreenEntity parent;

        ScrollingEntityList(int x, int y, int width, int height, List<EntityData> blocks, GuiSelectionScreenEntity parent) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.parent = parent;
        }

        public void setSelected(@Nullable EntitySlot entry, int mouse) {
            if (entry == null)
                return;

            if (GuiSelectionScreenEntity.hasShiftDown()) {
                Minecraft.getInstance().player.closeContainer();
                Minecraft.getInstance().setScreen(new EntityGuiEdit(entry.entity));
                return;
            }
            if (GuiSelectionScreenEntity.hasControlDown()) {
                if (ClientController.entityStore.AutoAimEntityTypes.contains(entry.entity.getEntityType())){
                    ClientController.entityStore.AutoAimEntityTypes.remove(entry.entity.getEntityType());
                }
                else {
                    ClientController.entityStore.AutoAimEntityTypes.add(entry.entity.getEntityType());
                }
                ClientController.entityStore.writeAutoAim();
                return;
            }

            ClientController.entityStore.toggleDrawing(entry.entity.getEntityType());
            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
        }

        void updateEntries(List<EntityData> entities) {
            this.clearEntries();
            entities.forEach(block -> this.addEntry(new EntitySlot(block, this))); // @mcp: addEntry = addEntry
        }

        public static class EntitySlot extends Entry<EntitySlot> {
            EntityData entity;
            ScrollingEntityList parent;

            EntitySlot(EntityData entity, ScrollingEntityList parent) {
                this.entity = entity;
                this.parent = parent;
            }

            public EntityData getEntity() {
                return entity;
            }

            @Override
            public void render(PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                EntityData entityData = this.entity;

                Font font = Minecraft.getInstance().font;

                font.draw(stack, entityData.getEntityType().toShortString(), left + 35, top + 7, 0xFFFFFF);
                font.draw(stack, entityData.isDrawing() ? "Enabled" : "Disabled", left + 35, top + 17, entity.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB());

//                Lighting.setupFor3DItems();
//                //Minecraft.getInstance().getEntityRenderDispatcher().getRenderer().render();
//                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(blockData.getItemStack(), left + 8, top + 7);
//                Lighting.setupForFlatItems();
                font.draw(stack, ClientController.entityStore.getAutoAimEntityTypes().contains(entityData.getEntityType()) ? "Aimed" : "NotAimed", left + 120, top + 17, ClientController.entityStore.getAutoAimEntityTypes().contains(entityData.getEntityType()) ? Color.GREEN.getRGB() : Color.RED.getRGB());

                if (mouseX > left && mouseX < (left + entryWidth) && mouseY > top && mouseY < (top + entryHeight) && mouseY < (this.parent.getTop() + this.parent.getHeight()) && mouseY > this.parent.getTop()) {
                    this.parent.parent.renderTooltip(
                            stack,
                            Language.getInstance().getVisualOrder(Arrays.asList(Component.translatable("xray.tooltips.edit1"), Component.translatable("xray.tooltips.edit2"),Component.translatable("entity.xray.edit2"))),
                            left + 15,
                            (entryIdx == this.parent.children().size() - 1 ? (top - (entryHeight - 20)) : (top + (entryHeight + 15))) // @mcp: children = getEntries
                    );
                }

                Color color = new Color(entityData.getColor());

                stack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderTexture(0, GuiSelectionScreenEntity.CIRCLE);
                RenderSystem.setShaderColor(0, 0, 0, .5f);
                blit(stack, (left + entryWidth) - 35, (int) (top + (entryHeight / 2f) - 9), 0, 0, 14, 14, 14, 14);
                RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
                blit(stack, (left + entryWidth) - 33, (int) (top + (entryHeight / 2f) - 7), 0, 0, 10, 10, 10, 10);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.disableBlend();
                stack.popPose();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}
