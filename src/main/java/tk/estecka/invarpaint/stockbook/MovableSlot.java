package tk.estecka.invarpaint.stockbook;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import tk.estecka.invarpaint.stockbook.mixin.ISlotMixin;

public class MovableSlot
extends Slot
{
	private boolean isVisible = true;
	
	public MovableSlot(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	public void SetX(int x){ ((ISlotMixin)this).setX(x); }
	public void SetY(int y){ ((ISlotMixin)this).setY(y); }
	public void SetVisible(boolean b){ this.isVisible = b; }

	@Override
	public boolean isEnabled(){ return this.isVisible; }
}
