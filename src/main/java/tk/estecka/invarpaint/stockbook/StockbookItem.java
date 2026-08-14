package tk.estecka.invarpaint.stockbook;

import java.util.function.Consumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import tk.estecka.invarpaint.stockbook.mixin.IItemsMixin;

public class StockbookItem
extends Item
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath("invarpaint", "stockbook");
	static public final Item ITEM = IItemsMixin.callRegisterItem(ResourceKey.create(Registries.ITEM, ID), StockbookItem::new, new Item.Properties().stacksTo(1));

	static public void Register() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(StockbookItem::CreativeInventory);
	}

	static private void CreativeInventory(FabricCreativeModeTabOutput entries){
		entries.insertAfter(Items.WRITABLE_BOOK, ITEM);

		final var registry = entries.getContext().holders().lookup(Registries.PAINTING_VARIANT);
		if (!registry.isPresent())
			return;

		ItemStack fullBook = new ItemStack(ITEM);

		Object2IntMap<PaintingEntry> everything = new Object2IntOpenHashMap<>();
		for (Holder<PaintingVariant> entry : registry.get().listElements().toList())
			everything.put(new PaintingEntry(entry), 1);

		fullBook.set(VariantCollectionComponent.TYPE, new VariantCollectionComponent(everything));
		fullBook.set(DataComponents.ITEM_NAME, Component.translatable("item.invarpaint.stockbook.name.complete"));
		fullBook.set(DataComponents.RARITY, Rarity.EPIC);
		fullBook.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		entries.insertAfter(ITEM, fullBook);
	}


	public StockbookItem(Item.Properties settings){
		super(settings);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);

		// Check might superfluous, especially if stockboocks are to become dyable.
		if (stack.is(ITEM)){
			player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
			player.openMenu(StockbookServerHandler.GetFactory(stack));
			return InteractionResult.SUCCESS;
		}
		else
			return InteractionResult.FAIL;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type){
		VariantCollectionComponent component = stack.get(VariantCollectionComponent.TYPE);
		if (component == null || component.content.isEmpty() || !displayComponent.shows(VariantCollectionComponent.TYPE))
			return;

		int stored=0, found=0;
		for (var entry : component.content.entrySet()){
			++found;
			stored += (entry.getValue() > 0) ? 1 : 0;
		}

		tooltip.accept(Component.translatable("item.invarpaint.stockbook.tooltip.content", stored, found).withStyle(ChatFormatting.GRAY));
	}
}
