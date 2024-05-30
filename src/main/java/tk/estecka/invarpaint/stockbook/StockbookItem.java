package tk.estecka.invarpaint.stockbook;

import java.util.List;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class StockbookItem
extends Item
{
	static public final Identifier ID = new Identifier("invarpaint", "stockbook");
	static public final Item ITEM = new StockbookItem( new Item.Settings().maxCount(1) );

	static public void Register() {
		Registry.register(Registries.ITEM, ID, ITEM);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(StockbookItem::CreativeInventory);
	}

	static private void CreativeInventory(FabricItemGroupEntries entries){
		ItemStack fullBook = new ItemStack(ITEM);
		
		Object2IntMap<Identifier> everything = new Object2IntOpenHashMap<>();
		for (Identifier id : Registries.PAINTING_VARIANT.getIds())
			everything.put(id, 1);

		fullBook.set(VariantCollectionComponent.TYPE, new VariantCollectionComponent(everything));
		fullBook.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.invarpaint.stockbook.name.complete"));
		fullBook.set(DataComponentTypes.RARITY, Rarity.EPIC);
		fullBook.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
	
		entries.addAfter(Items.WRITABLE_BOOK, ITEM);
		entries.addAfter(ITEM, fullBook);
	}


	public StockbookItem(Item.Settings settings){
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand){
		ItemStack stack = player.getStackInHand(hand);

		if (stack.isOf(ITEM)){
			player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
			player.openHandledScreen(StockbookServerHandler.GetFactory(stack));
			return TypedActionResult.success(stack);
		}
		else
			return TypedActionResult.fail(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type){
		VariantCollectionComponent component = stack.get(VariantCollectionComponent.TYPE);
		if (component == null || component.content.isEmpty())
			return;

		int stored=0, found=0;
		for (var entry : component.content.entrySet()){
			++found;
			stored += (entry.getValue() > 0) ? 1 : 0;
		}

		tooltip.add(Text.translatable("item.invarpaint.stockbook.tooltip.content", stored, found).formatted(Formatting.GRAY));
	}
}
