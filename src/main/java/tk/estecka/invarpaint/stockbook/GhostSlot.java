package tk.estecka.invarpaint.stockbook;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Slot for displaying placeholder items behind otherwise empty slots.
 */
public class GhostSlot
extends MovableSlot
{
	public GhostSlot(Container inv, int index, int x, int y){
		super(inv, index, x, y);
	}

	@Override
	public boolean mayPickup(Player player){
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack other){
		return false;
	}

	@Override
	public boolean isActive(){
		return super.isActive() && !this.getItem().isEmpty();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
