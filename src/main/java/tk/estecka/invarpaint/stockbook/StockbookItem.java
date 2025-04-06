package tk.estecka.invarpaint.stockbook;

import java.util.function.Consumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class StockbookItem
extends Item
{
	static public final Identifier ID = Identifier.of("invarpaint", "stockbook");
	static public final Item ITEM = Items.register(RegistryKey.of(RegistryKeys.ITEM, ID), StockbookItem::new, new Item.Settings().maxCount(1));

	static public void Register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(StockbookItem::CreativeInventory);
	}

	static private void CreativeInventory(FabricItemGroupEntries entries){
		entries.addAfter(Items.WRITABLE_BOOK, ITEM);

		final var registry = entries.getContext().lookup().getOptional(RegistryKeys.PAINTING_VARIANT);
		if (!registry.isPresent())
			return;

		ItemStack fullBook = new ItemStack(ITEM);

		Object2IntMap<PaintingEntry> everything = new Object2IntOpenHashMap<>();
		for (RegistryEntry<PaintingVariant> entry : registry.get().streamEntries().toList())
			everything.put(new PaintingEntry(entry), 1);

		fullBook.set(VariantCollectionComponent.TYPE, new VariantCollectionComponent(everything));
		fullBook.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.invarpaint.stockbook.name.complete"));
		fullBook.set(DataComponentTypes.RARITY, Rarity.EPIC);
		fullBook.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
		entries.addAfter(ITEM, fullBook);
	}


	public StockbookItem(Item.Settings settings){
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand){
		ItemStack stack = player.getStackInHand(hand);

		// Check might superfluous, especially if stockboocks are to become dyable.
		if (stack.isOf(ITEM)){
			player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
			player.openHandledScreen(StockbookServerHandler.GetFactory(stack));
			return ActionResult.SUCCESS;
		}
		else
			return ActionResult.FAIL;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type){
		VariantCollectionComponent component = stack.get(VariantCollectionComponent.TYPE);
		if (component == null || component.content.isEmpty() || !displayComponent.shouldDisplay(VariantCollectionComponent.TYPE))
			return;

		int stored=0, found=0;
		for (var entry : component.content.entrySet()){
			++found;
			stored += (entry.getValue() > 0) ? 1 : 0;
		}

		tooltip.accept(Text.translatable("item.invarpaint.stockbook.tooltip.content", stored, found).formatted(Formatting.GRAY));
	}
}
