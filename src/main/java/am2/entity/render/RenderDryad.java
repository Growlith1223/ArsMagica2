package am2.entity.render;

import am2.entity.EntityDryad;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDryad extends RenderBiped<EntityDryad>{

	private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/mobs/mob_dryad.png");

	public RenderDryad(RenderManager manager){
		super(manager, new ModelBiped(), 0.5f);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityDryad par1Entity){
		return rLoc;
	}

}
