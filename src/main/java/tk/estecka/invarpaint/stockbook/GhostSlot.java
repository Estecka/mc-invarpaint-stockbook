package tk.estecka.invarpaint.stockbook;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Slot for displaying placeholder items behind otherwise empty slots.
 */
public class GhostSlot
extends MovableSlot
{
	public GhostSlot(Inventory inv, int index, int x, int y){
		super(inv, index, x, y);
	}

	@Override
	public boolean canTakeItems(PlayerEntity player){
		return false;
	}

	@Override
	public boolean canInsert(ItemStack other){
		return false;
	}

	@Override
	public boolean isEnabled(){
		return super.isEnabled() && !this.getStack().isEmpty();
	}

	@Override
	public int getMaxItemCount() {
		return 1;
	}
}
