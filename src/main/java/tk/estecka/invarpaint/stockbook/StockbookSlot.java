package tk.estecka.invarpaint.stockbook;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class StockbookSlot
extends MovableSlot
{
	public final MovableSlot ghostSlot;
	
	public StockbookSlot(Inventory inv, int index, int x, int y, MovableSlot ghost){
		super(inv, index, x, y);
		this.ghostSlot = ghost;
	}

	public StockbookSlot(Inventory inv, int index, int x, int y, Inventory ghostView){
		super(inv, index, x, y);
		this.ghostSlot = new GhostSlot(ghostView, getIndex(), x, y);
	}

	@Override public void SetX(int x){ super.SetX(x); ghostSlot.SetX(x); }
	@Override public void SetY(int y){ super.SetY(y); ghostSlot.SetY(y); }
	@Override public void SetVisible(boolean b){ super.SetVisible(b); ghostSlot.SetVisible(b); }

	public PaintingEntry GetVariant(){
		ItemStack self = this.getStack();
		ItemStack ghost = ghostSlot.getStack();

		return !self.isEmpty()  ? StockbookInventory.Reduce(self)
		     : !ghost.isEmpty() ? StockbookInventory.Reduce(ghost)
		     : null
		     ;
	}

	@Override
	public boolean canInsert(ItemStack stack){
		PaintingEntry currentEntry = this.GetVariant();
		PaintingEntry stackEntry   = StockbookInventory.Reduce(stack);
		return stack.isOf(Items.PAINTING)
		    && stackEntry != null
		    && (currentEntry==null || currentEntry.equals(stackEntry))
		    ;
	}
}
