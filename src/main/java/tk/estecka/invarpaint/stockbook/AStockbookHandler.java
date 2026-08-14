package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class AStockbookHandler
extends AbstractContainerMenu
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath("invarpaint", "stockbook_handler");
	static MenuType.MenuSupplier<AStockbookHandler> clientFactory = (syncId,playInv)->{ throw new AssertionError("Not on client"); };
	static public final MenuType<AStockbookHandler> TYPE = new MenuType<>((syncId,playInv)->clientFactory.create(syncId,playInv), FeatureFlags.VANILLA_SET);
	static public void Register(){
		Registry.register(BuiltInRegistries.MENU, ID, TYPE);
	}


	/**
	 * The index  of the slot that  contains the container. This may be negative
	 * if the  container is  not in this  handler's  slots, i.e, in the player's
	 * off-hand.
	 */
	public final DataSlot containerSlot = DataSlot.standalone();

	// The main slots of the container itself.
	public final List<StockbookSlot> bookSlots = new ArrayList<>();

	protected final Inventory playerInventory;
	protected final Container bookInventory;
	protected final Container placeholdersInventory;
	protected final int playerEndIndex;


	protected AStockbookHandler(int syncId, Inventory inventory, Container bookView, Container ghostView){
		super(TYPE, syncId);
		this.playerInventory = inventory;
		this.bookInventory = bookView;
		this.placeholdersInventory = ghostView;

		this.addDataSlot(containerSlot);
		containerSlot.set(-1);

		for (int x=0; x<9; ++x) {
			this.addSlot(new MovableSlot(playerInventory, x, 0,0));
		}

		for (int y=0; y<3; ++y)
		for (int x=0; x<9; ++x)
		{
			int i = 9*y + x + 9;
			this.addSlot(new MovableSlot(playerInventory, i, 0,0));
		}

		this.playerEndIndex = this.slots.size();

		for (int i=0; i<bookView.getContainerSize(); ++i)
			this.AddBookSlot();

	}

	protected void AddBookSlot(){
		int i = this.bookSlots.size();
		GhostSlot ghost = new GhostSlot(this.placeholdersInventory, i, 0,0);
		StockbookSlot slot = new StockbookSlot(this.bookInventory, i, 0,0, ghost);
		this.addSlot(slot);
		this.addSlot(ghost);
		this.bookSlots.add(slot);
	}

	@Override
	public Slot addSlot(Slot slot){
		if (slot instanceof MovableSlot)
			return super.addSlot(slot);
		throw new IllegalArgumentException("Slot is not Movable");
	}

	@Override
	public MovableSlot getSlot(int i){
		return (MovableSlot)super.getSlot(i);
	}

	@Override
	public boolean stillValid(Player player){
		return bookInventory.stillValid(player);
	}

	@Override
	public void removed(Player player){
		super.removed(player);
		this.playerInventory.stopOpen(player);
	}

}
