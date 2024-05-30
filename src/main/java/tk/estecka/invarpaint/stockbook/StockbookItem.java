package tk.estecka.invarpaint.stockbook;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class StockbookItem
extends Item
{
	static public final String CONTENT_KEY = "invarpaint:stockbook_content";

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

		StockbookItem.SetContent(fullBook, everything);
		fullBook.setCustomName(Text.translatable("item.invarpaint.stockbook.name.complete"));
	
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
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext type){
		var content = GetContent(stack);
		if (content == null || content.isEmpty())
			return;

		int stored=0, found=0;
		for (var entry : content.object2IntEntrySet()){
			++found;
			stored += (entry.getIntValue() > 0) ? 1 : 0;
		}

		tooltip.add(Text.translatable("item.invarpaint.stockbook.tooltip.content", stored, found).formatted(Formatting.GRAY));
	}

	static public void SetContent(ItemStack stack, Object2IntMap<Identifier> content) {
		NbtCompound nbt = stack.getSubNbt(CONTENT_KEY);
		if (nbt == null)
			nbt = new NbtCompound();

		for (var entry : content.object2IntEntrySet())
		if  (entry.getKey() != null)
			nbt.putInt(entry.getKey().toString(), entry.getIntValue());

		if (!nbt.isEmpty())
			stack.setSubNbt(CONTENT_KEY, nbt);
	}

	static public @Nullable Object2IntMap<Identifier> GetContent(ItemStack stack){
		NbtCompound nbt = stack.getSubNbt(CONTENT_KEY);
		if (nbt == null)
			return null;

		Object2IntMap<Identifier> result = new Object2IntOpenHashMap<>();
		for (String rawVariant : nbt.getKeys()) {
			Identifier variantId = Identifier.tryParse(rawVariant);
			if (variantId == null)
				InvarpaintStockbookMod.LOGGER.error("Invalid id: {}", variantId);
			else if (!nbt.contains(rawVariant, NbtElement.NUMBER_TYPE))
				InvarpaintStockbookMod.LOGGER.error("Not a number: {}, {}", rawVariant, nbt.get(rawVariant));
			else
				result.put(variantId, nbt.getInt(rawVariant));
		}

		return result;
	}
}
