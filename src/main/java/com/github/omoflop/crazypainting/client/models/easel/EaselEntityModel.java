package com.github.omoflop.crazypainting.client.models.easel;

import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class EaselEntityModel extends EntityModel<EaselEntityRenderState>  {
    public EaselEntityModel(ModelPart root) {
        super(root.getChild("group"));
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition group = modelPartData.addOrReplaceChild("group", CubeListBuilder.create().texOffs(8, 49).addBox(-11.0F, -1.0F, -3.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-11.0F, -10.0F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 24.0F, -4.0F));

        PartDefinition arm3_r1 = group.addOrReplaceChild("arm3_r1", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -28.0F, -1.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 8.0F, 0.1309F, 0.0F, 0.0F));
        PartDefinition arm2_r1 = group.addOrReplaceChild("arm2_r1", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -28.0F, -1.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.096F, 0.0F, -0.1134F));
        PartDefinition arm1_r1 = group.addOrReplaceChild("arm1_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -28.0F, -1.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, -0.096F, 0.0F, 0.1134F));

        return LayerDefinition.create(modelData, 64, 64);
    }
}
