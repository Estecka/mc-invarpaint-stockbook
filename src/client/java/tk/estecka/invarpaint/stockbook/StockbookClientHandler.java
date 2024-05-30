package tk.estecka.invarpaint.stockbook;

import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;


public class StockbookClientHandler
extends AStockbookHandler
{
	public @Nullable Identifier requestedFocus = null;

	public StockbookClientHandler(int syncId, PlayerInventory player){
		super(syncId, player, new VariableSizeInventory(), new VariableSizeInventory());
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex){
		if (slotIndex < this.playerEndIndex)
			this.requestedFocus = StockbookInventory.Reduce(this.getSlot(slotIndex).getStack());

		return ItemStack.EMPTY;
	}

	/**
	 * A slot out of bound is  assumed to mean that the  server's representation
	 * of the  stockbook  has grown  larger than the client's, which is expected
	 * when registering new paintings to it.
	 */
	@Override
	public MovableSlot getSlot(int index){
		int reqSize = index + 1;
		int max = this.playerEndIndex + 2*Registries.PAINTING_VARIANT.size();
		if (max < reqSize && this.slots.size() <= max)
			InvarpaintStockbookMod.LOGGER.warn("Stockbook exceeded the expected maximum size of {} by {} ", max, reqSize-max);

		while (this.slots.size() < reqSize)
			super.AddBookSlot();

		return super.getSlot(index);
	}
}
