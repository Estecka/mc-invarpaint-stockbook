package tk.estecka.invarpaint.stockbook;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import tk.estecka.invarpaint.PaintStackUtil;

public class StockbookServerHandler
extends AStockbookHandler
{
	private final StockbookInventory bookInventory;
	private final ItemStack container;

	public StockbookServerHandler(int syncId, PlayerInventory playerInventory, ItemStack container){
		this(syncId, playerInventory, new StockbookInventory(container), container);
		for (int i=0; i<this.playerEndIndex; ++i)
		if  (container == playerInventory.getStack(i)){
			this.containerSlot.set(i);
			this.sendContentUpdates();
			break;
		}
	}

	private StockbookServerHandler(int syncId, PlayerInventory playerInventory, StockbookInventory bookInventory, ItemStack container){
		super(syncId, playerInventory, bookInventory, bookInventory.ghostView);
		this.bookInventory = bookInventory;
		this.bookInventory.handler = this;
		this.container = container;
	}


	static public SimpleNamedScreenHandlerFactory GetFactory(ItemStack bookStack){
		return new SimpleNamedScreenHandlerFactory((syncId,inventory,player)-> new StockbookServerHandler(syncId, inventory, bookStack), Text.literal("Stockbook"));
	}

	@Override
	public void onContentChanged(Inventory inventory){
		while (this.bookSlots.size() < this.bookInventory.size())
			this.AddBookSlot();
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotId){
		Slot slot = this.getSlot(slotId);
		ItemStack stack  = slot.getStack();

		if (!stack.isOf(Items.PAINTING) || !PaintStackUtil.HasVariantId(stack))
			return ItemStack.EMPTY;

		if (slot.inventory == this.playerInventory){
			ItemStack remainder = bookInventory.TryInsert(stack);
			if (remainder != stack){
				slot.setStack(remainder);
				bookInventory.markDirty();
				return stack;
			}
		}
		else if (
			slot.inventory == this.bookInventory 
			&& slot.canTakeItems(player)
			&& this.insertItem(stack, 0, this.playerEndIndex, false)
		){
			bookInventory.setStack(slot.getIndex(), stack);
			bookInventory.markDirty();
			return stack;
		}

		return ItemStack.EMPTY;
	}

	private void LocateContainer(){
		this.containerSlot.set(-1);

		for (int i=0; i<playerEndIndex; ++i)
		if  (this.slots.get(i).getStack() == this.container)
			this.containerSlot.set(i);

		this.sendContentUpdates();
	}

	/**
	 * Overrides pick-up actions involving the container, ensuring that its
	 * identity is preserved so it can always be tracked.
	 * See {@link StockbookInventory#canPlayerUse}
	 */
	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType action, PlayerEntity player){
		// Very hacky. No idea how this would behave if books were stackable.
		if (action == SlotActionType.QUICK_CRAFT && slotIndex > 0 && this.getCursorStack() == this.container)
			action = SlotActionType.PICKUP;

		if (action != SlotActionType.PICKUP || slotIndex < 0 || slots.size() <= slotIndex){
			super.onSlotClick(slotIndex, button, action, player);
			this.LocateContainer();
			return;
		}
		
		Slot slot = this.slots.get(slotIndex);
		if (this.getCursorStack() == this.container){
			this.setCursorStack(slot.getStack());
			slot.setStack(container);
		}
		else if (slot.getStack() == this.container){
			slot.setStack(this.getCursorStack());
			this.setCursorStack(container);
		}
		else
			super.onSlotClick(slotIndex, button, action, player);

		this.LocateContainer();
	}
}
