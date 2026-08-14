package tk.estecka.invarpaint.stockbook;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StockbookServerHandler
extends AStockbookHandler
{
	private final StockbookInventory bookInventory;
	private final ItemStack container;

	public StockbookServerHandler(int syncId, Inventory playerInventory, ItemStack container){
		this(syncId, playerInventory, new StockbookInventory(container), container);
		for (int i=0; i<this.playerEndIndex; ++i)
		if  (container == playerInventory.getItem(i)){
			this.containerSlot.set(i);
			this.broadcastChanges();
			break;
		}
	}

	private StockbookServerHandler(int syncId, Inventory playerInventory, StockbookInventory bookInventory, ItemStack container){
		super(syncId, playerInventory, bookInventory, bookInventory.ghostView);
		this.bookInventory = bookInventory;
		this.bookInventory.handler = this;
		this.container = container;
	}


	static public SimpleMenuProvider GetFactory(ItemStack bookStack){
		return new SimpleMenuProvider((syncId,inventory,player)-> new StockbookServerHandler(syncId, inventory, bookStack), Component.literal("Stockbook"));
	}

	@Override
	public void slotsChanged(Container inventory){
		while (this.bookSlots.size() < this.bookInventory.getContainerSize())
			this.AddBookSlot();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotId){
		Slot slot = this.getSlot(slotId);
		ItemStack stack  = slot.getItem();

		if (StockbookInventory.Reduce(stack) == null)
			return ItemStack.EMPTY;

		if (slot.container == this.playerInventory){
			ItemStack remainder = bookInventory.TryInsert(stack);
			if (remainder != stack){
				slot.setByPlayer(remainder);
				bookInventory.setChanged();
				return stack;
			}
		}
		else if (
			slot.container == this.bookInventory 
			&& slot.mayPickup(player)
			&& this.moveItemStackTo(stack, 0, this.playerEndIndex, false)
		){
			bookInventory.setItem(slot.getContainerSlot(), stack);
			bookInventory.setChanged();
			return stack;
		}

		return ItemStack.EMPTY;
	}

	private void LocateContainer(){
		this.containerSlot.set(-1);

		for (int i=0; i<playerEndIndex; ++i)
		if  (this.slots.get(i).getItem() == this.container)
			this.containerSlot.set(i);

		this.broadcastChanges();
	}

	/**
	 * Overrides pick-up actions involving the container, ensuring that its
	 * identity is preserved so it can always be tracked.
	 * See {@link StockbookInventory#stillValid}
	 */
	@Override
	public void clicked(int slotIndex, int button, ContainerInput action, Player player){
		// Disable quick-craft for the container. This action would definitely change the stack's identity, and could even mutate the container into air.
		// Very hacky. No idea how this would behave if books were stackable.
		if (action == ContainerInput.QUICK_CRAFT && slotIndex > 0 && this.getCarried() == this.container)
			action = ContainerInput.PICKUP;

		Slot slot = null;
		if (0 <= slotIndex && slotIndex < slots.size())
			slot = this.getSlot(slotIndex);

		if (action == ContainerInput.PICKUP
		&& slot != null
		&& (this.getCarried() == container || slot.getItem() == container)
		&& slot.mayPlace(this.getCarried())
		){
			// Same behaviour as vanilla, but enforces preservation of pointers.
			ItemStack swap = slot.getItem();
			slot.setByPlayer(this.getCarried());
			this.setCarried(swap);
		}
		else
			super.clicked(slotIndex, button, action, player);

		this.LocateContainer();
	}
}
