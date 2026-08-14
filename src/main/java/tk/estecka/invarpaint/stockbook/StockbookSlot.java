package tk.estecka.invarpaint.stockbook;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StockbookSlot
extends MovableSlot
{
	public final MovableSlot ghostSlot;
	
	public StockbookSlot(Container inv, int index, int x, int y, MovableSlot ghost){
		super(inv, index, x, y);
		this.ghostSlot = ghost;
	}

	public StockbookSlot(Container inv, int index, int x, int y, Container ghostView){
		super(inv, index, x, y);
		this.ghostSlot = new GhostSlot(ghostView, getContainerSlot(), x, y);
	}

	@Override public void SetX(int x){ super.SetX(x); ghostSlot.SetX(x); }
	@Override public void SetY(int y){ super.SetY(y); ghostSlot.SetY(y); }
	@Override public void SetVisible(boolean b){ super.SetVisible(b); ghostSlot.SetVisible(b); }

	public PaintingEntry GetVariant(){
		ItemStack self = this.getItem();
		ItemStack ghost = ghostSlot.getItem();

		return !self.isEmpty()  ? StockbookInventory.Reduce(self)
		     : !ghost.isEmpty() ? StockbookInventory.Reduce(ghost)
		     : null
		     ;
	}

	@Override
	public boolean mayPlace(ItemStack stack){
		PaintingEntry currentEntry = this.GetVariant();
		PaintingEntry stackEntry   = StockbookInventory.Reduce(stack);
		return stack.is(Items.PAINTING)
		    && stackEntry != null
		    && (currentEntry==null || currentEntry.equals(stackEntry))
		    ;
	}
}
