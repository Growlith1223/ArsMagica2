package am2.texture;

import java.util.HashMap;

import am2.api.ArsMagicaAPI;
import am2.api.skill.Skill;
import am2.gui.AMGuiIcons;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpellIconManager {
	
	public static final SpellIconManager INSTANCE = new SpellIconManager();
	
	private final HashMap<String, TextureAtlasSprite> sprites;
	
	private SpellIconManager() {
		this.sprites = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void init (TextureStitchEvent.Pre e) {
		sprites.clear();
		AMGuiIcons.instance.init(e.getMap());
		for (Skill skill : ArsMagicaAPI.getSkillRegistry().getValues()) {
			if (skill.getIcon() != null)
				sprites.put(skill.getID(), e.getMap().registerSprite(skill.getIcon()));
		}
		sprites.put("CasterRuneSide", e.getMap().registerSprite(new ResourceLocation("arsmagica2:blocks/caster_rune_side")));
		sprites.put("RuneStone", e.getMap().registerSprite(new ResourceLocation("arsmagica2:blocks/rune_stone")));
	}
	
	public TextureAtlasSprite getSprite(String name) {
		return sprites.get(name);
	}
	
}
