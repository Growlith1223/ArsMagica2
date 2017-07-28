package am2.entity.render;

import am2.entity.EntityManaElemental;
import am2.entity.models.ModelManaElemental;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderManaElemental extends RenderBiped<EntityManaElemental>{

	private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/mobs/mana_elemental.png");

	public RenderManaElemental(RenderManager renderManager){
		super(renderManager, new ModelManaElemental(), 0.5f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityManaElemental par1Entity){
		return rLoc;
	}

}
