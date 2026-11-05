package com.example.gulp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

/** Belly bulge for MCA villagers that have a player in their stomach (same look as the player belly). */
public class VillagerBellyLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
   private final ModelPart bellyFull;
   private final ModelPart jacketFull;
   private final ModelPart bellyLow;
   private final ModelPart jacketLow;
   private final ModelPart bellyFullClothing;
   private final ModelPart jacketFullClothing;
   private final ModelPart bellyLowClothing;
   private final ModelPart jacketLowClothing;
   private final RenderLayerParent<T, M> parent;
   private List<Object> mcaLayers;

   public VillagerBellyLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
      super(parent);
      ModelPart root = models.m_171103_(ClientModEvents.BELLY_LAYER);
      this.bellyFull = root.m_171324_("belly_full");
      this.jacketFull = root.m_171324_("jacket_full");
      this.bellyLow = root.m_171324_("belly_low");
      this.jacketLow = root.m_171324_("jacket_low");
      this.bellyFullClothing = root.m_171324_("belly_full_clothing");
      this.jacketFullClothing = root.m_171324_("jacket_full_clothing");
      this.bellyLowClothing = root.m_171324_("belly_low_clothing");
      this.jacketLowClothing = root.m_171324_("jacket_low_clothing");
      this.parent = parent;
   }

   public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      if (!entity.m_20145_()) {
         float shown = ClientState.smoothedBelly(entity.m_20148_());
         if (!(shown < 0.02F)) {
            if (GulpConfig.bellyStyle != GulpConfig.BellyStyle.OFF) {
               boolean classic = GulpConfig.bellyStyle == GulpConfig.BellyStyle.CLASSIC;
               ModelPart belly = classic ? this.bellyFull : this.bellyLow;
               ModelPart jacket = classic ? this.jacketFull : this.jacketLow;
               float bulge = (float)Math.sqrt((double)shown);
               float depthScale = 1.0F + bulge * 1.6F;
               float widthScale = 1.0F + bulge * 0.18F;
               poseStack.m_85836_();
               this.m_117386_().f_102810_.m_104299_(poseStack);
               poseStack.m_252880_(0.0F, 0.0F, 0.125F);
               poseStack.m_85841_(widthScale, 1.0F, depthScale);
               poseStack.m_252880_(0.0F, 0.0F, -0.125F);
               int overlay = LivingEntityRenderer.m_115338_(entity, 0.0F);
               if (this.mcaLayers == null) {
                  this.mcaLayers = McaSkins.findLayers(this.parent);
               }

               List<McaSkins.Pass> mca = McaSkins.passes(entity, this.mcaLayers, partialTick);
               if (mca != null && !mca.isEmpty()) {
                  for(McaSkins.Pass pass : mca) {
                     VertexConsumer mcaBuffer = buffer.m_6299_(RenderType.m_110458_(pass.texture()));
                     ModelPart body = pass.clothing() ? (classic ? this.bellyFullClothing : this.bellyLowClothing) : belly;
                     ModelPart wear = pass.clothing() ? (classic ? this.jacketFullClothing : this.jacketLowClothing) : jacket;
                     body.m_104306_(poseStack, mcaBuffer, packedLight, overlay, pass.r(), pass.g(), pass.b(), 1.0F);
                     wear.m_104306_(poseStack, mcaBuffer, packedLight, overlay, pass.r(), pass.g(), pass.b(), 1.0F);
                  }
               } else {
                  VertexConsumer vc = buffer.m_6299_(RenderType.m_110458_(this.parent.m_5478_(entity)));
                  belly.m_104301_(poseStack, vc, packedLight, overlay);
                  jacket.m_104301_(poseStack, vc, packedLight, overlay);
               }

               poseStack.m_85849_();
            }
         }
      }
   }
}
