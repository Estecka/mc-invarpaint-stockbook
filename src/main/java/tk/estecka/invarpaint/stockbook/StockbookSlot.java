package tk.estecka.invarpaint.stockbook;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import static tk.estecka.invarpaint.core.PaintStackUtil.HasVariantId;

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

	public Identifier GetVariant(){
		ItemStack self = this.getStack();
		ItemStack ghost = ghostSlot.getStack();

		return !self.isEmpty()  ? StockbookInventory.Reduce(self)
		     : !ghost.isEmpty() ? StockbookInventory.Reduce(ghost)
		     : null
		     ;
	}

	@Override
	public boolean canInsert(ItemStack other){
		Identifier acceptable = this.GetVariant();
		Identifier incoming   = StockbookInventory.Reduce(other);
		return other.isOf(Items.PAINTING)
		    && HasVariantId(other)
		    && (acceptable==null || acceptable.equals(incoming))
		    ;
	}
}
