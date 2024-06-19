package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public abstract class AStockbookHandler
extends ScreenHandler
{
	static public final Identifier ID = Identifier.of("invarpaint", "stockbook_handler");
	static ScreenHandlerType.Factory<AStockbookHandler> clientFactory = (syncId,playInv)->{ throw new AssertionError("Not on client"); };
	static public final ScreenHandlerType<AStockbookHandler> TYPE = new ScreenHandlerType<>((syncId,playInv)->clientFactory.create(syncId,playInv), FeatureFlags.VANILLA_FEATURES);
	static public void Register(){
		Registry.register(Registries.SCREEN_HANDLER, ID, TYPE);
	}


	/**
	 * The index  of the slot that  contains the container. This may be negative
	 * if the  container is  not in this  handler's  slots, i.e, in the player's
	 * off-hand.
	 */
	public final Property containerSlot = Property.create();

	// The main slots of the container itself.
	public final List<StockbookSlot> bookSlots = new ArrayList<>();

	protected final PlayerInventory playerInventory;
	protected final Inventory bookInventory;
	protected final Inventory placeholdersInventory;
	protected final int playerEndIndex;


	protected AStockbookHandler(int syncId, PlayerInventory inventory, Inventory bookView, Inventory ghostView){
		super(TYPE, syncId);
		this.playerInventory = inventory;
		this.bookInventory = bookView;
		this.placeholdersInventory = ghostView;

		this.addProperty(containerSlot);
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

		for (int i=0; i<bookView.size(); ++i)
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
	public boolean canUse(PlayerEntity player){
		return bookInventory.canPlayerUse(player);
	}

	@Override
	public void onClosed(PlayerEntity player){
		super.onClosed(player);
		this.playerInventory.onClose(player);
	}

}
