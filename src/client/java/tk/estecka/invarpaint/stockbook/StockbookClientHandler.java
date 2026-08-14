package tk.estecka.invarpaint.stockbook;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;


public class StockbookClientHandler
extends AStockbookHandler
{
	public @Nullable PaintingEntry requestedFocus = null;

	public StockbookClientHandler(int syncId, Inventory player){
		super(syncId, player, new VariableSizeInventory(), new VariableSizeInventory());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex){
		if (slotIndex < this.playerEndIndex)
			this.requestedFocus = StockbookInventory.Reduce(this.getSlot(slotIndex).getItem());

		return ItemStack.EMPTY;
	}

	/**
	 * A slot out of bound is  assumed to mean that the  server's representation
	 * of the  stockbook  has grown  larger than the client's, which is expected
	 * when registering new paintings to it.
	 */
	@Override
	public MovableSlot getSlot(int index){
		final var registry = playerInventory.player.level().registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT);
		int reqSize = index + 1;
		int max = this.playerEndIndex + 2*registry.size();
		if (max < reqSize && this.slots.size() <= max)
			InvarpaintStockbookMod.LOGGER.warn("Stockbook exceeded the expected maximum size of {} by {} ", max, reqSize-max);

		while (this.slots.size() < reqSize)
			super.AddBookSlot();

		return super.getSlot(index);
	}
}
