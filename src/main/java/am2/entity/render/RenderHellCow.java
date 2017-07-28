package am2.entity.render;

import am2.entity.EntityHellCow;
import am2.models.ModelHellCow;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderHellCow extends RenderBiped<EntityHellCow>{

	private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/mobs/hell_cow.png");

	public RenderHellCow(RenderManager manager){
		super(manager, new ModelHellCow(), 0.5f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHellCow entity){
		return rLoc;
	}
}
