package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import fr.estecka.invarpaint.api.PaintStackUtil;


public class StockbookInventory
implements Container
{
	public final GhostView ghostView = new GhostView();
	private final ItemStack container;

	/**
	 * Zero  is  a valid  amount  to hold and  save. This  indicates  a painting
	 * variant that  has been discovered, despite no longer  being stored in the
	 * book.
	 */
	private final Object2IntMap<@NotNull PaintingEntry> content = new Object2IntOpenHashMap<>();
	/**
	 * For the most predictable  results on the  clientside, the variant in each
	 * slot  should remain  constant  for the lifetime  of the screen. I.e. only
	 * add, not set.
	 */
	private final List<@NotNull PaintingEntry> layout = new ArrayList<>();

	public @Nullable AbstractContainerMenu handler;

	public StockbookInventory(ItemStack container){
		this.container = container;
		this.ReloadItem();
	}

	/**
	 * @return null if the item is invalid to hold, otherwise, its variant entry.
	 */
	static public @Nullable PaintingEntry Reduce(ItemStack stack){
		PaintingEntry variant = null;

		var variantEntry = PaintStackUtil.GetVariantEntry(stack);
		if (stack.is(Items.PAINTING) && variantEntry!=null)
			variant = new PaintingEntry(variantEntry);

		return variant;
	}

	/**
	 * This function also keeps the layout up to date. It should always be used
	 * when inserting new identifiers into the content.
	 */
	public void SetStack(@NotNull PaintingEntry variant, int count){
		assert variant != null;

		if (!this.layout.contains(variant))
			this.layout.add(variant);
		this.content.put(variant, count);
	}

	@Override
	public int getContainerSize(){
		return this.layout.size();
	}

	@Override
	public boolean isEmpty() {
		return layout.isEmpty();
	}

	@Override
	public void clearContent(){
		this.content.clear();
		this.layout.clear();
	}

	@Override
	public int getMaxStackSize(){
		return Integer.MAX_VALUE;
	}

	public @Nullable PaintingEntry GetVariant(int i){
		return i<layout.size() ? layout.get(i) : null;
	}

	@Override
	public ItemStack getItem(int i){
		if (i >= layout.size() || layout.get(i) == null)
			return ItemStack.EMPTY;
		else
			return this.getStack(this.layout.get(i));
	}

	public ItemStack getStack(PaintingEntry variant){
		var stack = new ItemStack(Items.PAINTING);

		if (variant != null)
			PaintStackUtil.SetVariant(stack, variant.entry());

		int count = content.getInt(variant);
		stack.setCount(count);
		return stack;
	}

	public boolean isGhost(int i){
		return i<layout.size() 
		    && layout.get(i) != null
		    && this.content.getInt(layout.get(i)) <= 0
		    ;
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int i){
		PaintingEntry variant  = layout.get(i);

		ItemStack stack = PaintStackUtil.CreateVariant(variant.entry());
		stack.setCount(content.getInt(variant));
		this.SetStack(variant, 0);
		this.setChanged();
		return stack;
	}
	
	@Override
	public ItemStack removeItem(int i, int amount){
		PaintingEntry variant = layout.get(i);
		int stored = content.getInt(variant);
		amount = Math.min(amount, stored);
		
		this.SetStack(variant, stored - amount);
		this.setChanged();

		ItemStack stack = PaintStackUtil.CreateVariant(variant.entry());
		stack.setCount(amount);
		return stack;
	}

	/**
	 * This counts as both an insertion and a removal. This MUST NOT combine the
	 * incoming stack with the current stack in the same slot. However this must
	 * still combine the incoming stack with another stack in a different slot.
	 */
	@Override
	public void setItem(int i, ItemStack stack){
		@Nullable PaintingEntry neoVariant = Reduce(stack);
		PaintingEntry oldVariant = this.layout.get(i);
		if (!stack.isEmpty() && neoVariant == null)
			throw new IllegalArgumentException("An invalid item has been inserted into a stockbook: "+stack.toString());

		if (!oldVariant.equals(neoVariant))
			stack.grow(this.content.getInt(oldVariant));

		this.SetStack(oldVariant, 0);
		if (neoVariant != null)
			this.SetStack(neoVariant, stack.getCount());
		this.setChanged();
	}

	/**
	 * @return The remainder, or the original if nothing was inserted.
	 */
	public ItemStack TryInsert(ItemStack incoming){
		PaintingEntry variantId = Reduce(incoming);
		if (variantId == null)
			return incoming;

		int max = Items.PAINTING.getDefaultMaxStackSize();
		int stored = this.content.getInt(variantId);
		if (stored >= max)
			return incoming;

		int total = stored + incoming.getCount();
		if (total <= max){
			this.SetStack(variantId, total);
			incoming = ItemStack.EMPTY;
		}
		else {
			this.SetStack(variantId, max);
			incoming.copy();
			incoming.shrink(max - stored);
		}

		this.setChanged();
		return incoming;
	}

	@Override
	public void setChanged(){
		this.container.set(VariantCollectionComponent.TYPE, new VariantCollectionComponent(this.content));
		if (handler != null)
			handler.slotsChanged(this);
	}

	/**
	 * Checks that the player's inventory contains the container stack.
	 * 
	 * @implNote Equality should be verified by pointers !!
	 * If the original stack is thrown out of the inventory, an item stack that
	 * is identical should still be considered different !
	 */
	@Override
	public boolean stillValid(Player player){
		Container inv = player.getInventory();

		if (this.container.isEmpty())
			return false;
		if (player.containerMenu.getCarried() == this.container)
			return true;
		else for (int i=0; i<inv.getContainerSize(); ++i) {
			if (inv.getItem(i) == this.container)
				return true;
		}

		return false;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack){
		PaintingEntry incoming = Reduce(stack);
		PaintingEntry acceptable = layout.get(slot);
		return incoming != null && (acceptable == null || incoming.equals(acceptable));
	}

	public void ReloadItem(){
		this.content.clear();
		this.layout.clear();

		var neoContent = this.container.get(VariantCollectionComponent.TYPE);
		if  (neoContent != null)
		for (var entry : neoContent.content.entrySet())
			this.SetStack(entry.getKey(), entry.getValue());
	}

	public class GhostView
	implements Container
	{
		private final StockbookInventory parent = StockbookInventory.this;

		public int getContainerSize(){
			return parent.getContainerSize();
		}
		public boolean canPlaceItem(int i, ItemStack stack){
			return parent.canPlaceItem(i, stack);
		}
		public boolean stillValid(Player player){
			return parent.stillValid(player);
		}
		
		public int getMaxStackSize(){
			return 1;
		}

		public ItemStack getItem(int i){
			if (parent.isGhost(i))
				return PaintStackUtil.CreateVariant(parent.GetVariant(i).entry());
			else
				return ItemStack.EMPTY;
		}

		public void setItem(int i, ItemStack stack){}
		public void setChanged(){}
		public void clearContent(){}
		public boolean isEmpty(){ return true; }
		public boolean canTakeItem(Container hopper, int slot, ItemStack stack){ return false; }
		public ItemStack removeItemNoUpdate(int index){ return ItemStack.EMPTY; }
		public ItemStack removeItem(int index, int amount){ return ItemStack.EMPTY; }
	}
}
