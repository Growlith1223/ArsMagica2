package am2.api.compendium;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public final class CompendiumCategory {
	
	private static final ArrayList<CompendiumCategory> CATEGORIES = new ArrayList<>();

	public static final CompendiumCategory GUIDE = createCompendiumCategory("guide", new ResourceLocation("arsmagica2", "items/arcane_compendium"));
	public static final CompendiumCategory MECHANIC = createCompendiumCategory("mechanic", new ResourceLocation("arsmagica2", "items/magitech_goggle"));
	public static final CompendiumCategory MECHANIC_AFFINITY = createCompendiumCategory("mechanic.affinity", new ResourceLocation("missingno"));
	public static final CompendiumCategory MECHANIC_ENCHANTS = createCompendiumCategory("mechanic.enchants", new ResourceLocation("missingno"));
	public static final CompendiumCategory MECHANIC_INFUSIONS = createCompendiumCategory("mechanic.infusions", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM = createCompendiumCategory("item", new ResourceLocation("arsmagica2", "items/essence_ice"));
	public static final CompendiumCategory ITEM_ORE = createCompendiumCategory("item.ore", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_ESSENCE = createCompendiumCategory("item.essence", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_RUNE = createCompendiumCategory("item.rune", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_ARMOR = createCompendiumCategory("item.armor", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_ARMOR_MAGE = createCompendiumCategory("item.armor.mage", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_ARMOR_BATTLEMAGE = createCompendiumCategory("item.armor.battlemage", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_AFFINITYTOME = createCompendiumCategory("item.affinity_tomes", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_FOCI = createCompendiumCategory("item.foci", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_MANAPOTION = createCompendiumCategory("item.mana_potion", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_BINDINGCATALYST = createCompendiumCategory("item.binding_catalyst", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_FLICKERFOCUS = createCompendiumCategory("item.flickerfocus", new ResourceLocation("missingno"));
	public static final CompendiumCategory ITEM_INSCRIPTIONUPGRADES = createCompendiumCategory("item.inscription_upgrades", new ResourceLocation("missingno"));
	public static final CompendiumCategory BLOCK = createCompendiumCategory("block", new ResourceLocation("arsmagica2", "items/crystal_wrench"));
	public static final CompendiumCategory BLOCK_ILLUSIONBLOCKS = createCompendiumCategory("block.illusionblocks", new ResourceLocation("missingno"));
	public static final CompendiumCategory BLOCK_CRYSTALMARKER = createCompendiumCategory("block.crystalmarker", new ResourceLocation("missingno"));
	public static final CompendiumCategory BLOCK_INLAYS = createCompendiumCategory("block.inlays", new ResourceLocation("missingno"));
	public static final CompendiumCategory SPELL_SHAPE = createCompendiumCategory("shape", new ResourceLocation("arsmagica2", "items/spells/shapes/binding"));
	public static final CompendiumCategory SPELL_COMPONENT = createCompendiumCategory("component", new ResourceLocation("arsmagica2", "items/spells/components/lifetap"));
	public static final CompendiumCategory SPELL_MODIFIER = createCompendiumCategory("modifier", new ResourceLocation("arsmagica2", "items/spells/modifiers/velocityadded"));
	public static final CompendiumCategory TALENT = createCompendiumCategory("talent", new ResourceLocation("arsmagica2", "items/spells/skills/augmentedcasting"));
	public static final CompendiumCategory MOB = createCompendiumCategory("mob", new ResourceLocation("arsmagica2", "gui_icons/fatigue_icon"));
	public static final CompendiumCategory MOB_FLICKER = createCompendiumCategory("mob.flicker", new ResourceLocation("missingno"));
	public static final CompendiumCategory STRUCTURE = createCompendiumCategory("structure", new ResourceLocation("arsmagica2", "gui_icons/gateway"));
	public static final CompendiumCategory BOSS = createCompendiumCategory("boss", new ResourceLocation("arsmagica2", "items/evil_book"));
	
	private ArrayList<CompendiumEntry> entries;
	
	public static CompendiumCategory createCompendiumCategory(String id, ResourceLocation sprite) {
		if (id == null || id.isEmpty()) throw new NullPointerException("ID Can\'t be null");
		int num = id.split("\\.").length - 1;
		String[] parents = new String[num];
		for (int i = 0; i < num; i++) {
			parents[i] = id.split("\\.")[i];
		}
		CompendiumCategory category = new CompendiumCategory(id.split("\\.")[num], sprite, parents);
		CATEGORIES.add(category);
		
		return category;
	}
	
	private String id;
	private ResourceLocation sprite;
	private String[] parents;
	
	private CompendiumCategory(String id, ResourceLocation sprite, String[] parents) {
		this.id = id;
		this.sprite = sprite;
		this.entries = new ArrayList<>();
		this.parents = parents;
	}
	
	public boolean hasParents() {
		return parents.length > 0;
	}
	
	public String[] getParents() {
		return parents;
	}
	
	public String getParentsString() {
		String str = "";
		boolean first = true;
		for (String p : parents) {
			if (first)
				str += p;
			else
				str += "." + p;
			first = false;
		}
		return str;
	}
	
	public String getID() {
		return (hasParents() ? getParentsString() + "." : "") + id;
	}
	
	public ResourceLocation getSprite() {
		return sprite;
	}
	
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTexture() {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getSprite().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public void registerTexture(TextureMap map) {
		map.registerSprite(getSprite());
	}
	
	public void addEntry(CompendiumEntry entry) {
		entries.add(entry.setCategory(this));
	}
	
	public String getCategoryName() {
		return I18n.translateToLocal("compendium.category." + getID() + ".name");
	}
	
	public static ImmutableList<CompendiumCategory> getCategories() {
		return ImmutableList.copyOf(CATEGORIES);
	}
	
	public static CompendiumEntry getEntryByID(String id) {
		for (CompendiumCategory category : CompendiumCategory.getCategories()) {
			for (CompendiumEntry entry : category.getEntries()) {
				if (entry.getID().equals(id))
					return entry;
			}
		}
		return null;
	}
	
	public static CompendiumCategory getCategoryFromID(String id) {
		for (CompendiumCategory category : CATEGORIES) {
			if (category.getID().equals(id))
				return category;
		}
		return null;
	}
	
	public static ImmutableList<CompendiumEntry> getAllEntries() {
		ArrayList<CompendiumEntry> builder = new ArrayList<>();
		for (CompendiumCategory category : CompendiumCategory.getCategories()) {
			builder.addAll(category.getEntries());
		}
		return ImmutableList.copyOf(builder);
	}
	
	public ImmutableList<CompendiumEntry> getEntries() {
		return ImmutableList.copyOf(entries);
	}
}
