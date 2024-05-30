package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;


public class VariableSizeInventory
implements Inventory
{
	private final List<ItemStack> content =  new ArrayList<>();

	public VariableSizeInventory(){
	}

	@Override
	public int size(){
		return this.content.size();
	}

	@Override
	public boolean isEmpty(){
		for (ItemStack stack : this.content)
		if  (!stack.isEmpty())
			return false;

		return true;
	}

	@Override
	public ItemStack getStack(int i){
		return i < content.size() ? content.get(i) : ItemStack.EMPTY;
	}

	@Override
	public void setStack(int i, ItemStack stack){
		while (content.size() <= i)
			this.content.add(ItemStack.EMPTY);

		this.content.set(i, stack);
	}

	@Override
	public ItemStack removeStack(int i){
		ItemStack r = ItemStack.EMPTY;
		if (i < content.size()) {
			r = content.get(i);
			content.set(i, ItemStack.EMPTY);
		}
		return r;
	}

	@Override
	public ItemStack removeStack(int i, int amount){
		ItemStack r = ItemStack.EMPTY;

		if (i < content.size()) {
			ItemStack original = content.get(i);
			r = original.copyWithCount(amount);
			original.decrement(amount);
			content.set(i, original);
		}

		return r;
	}

	@Override
	public void clear(){
		this.content.clear();
	}

	@Override
	public void markDirty(){
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player){
		return true;
	}
}
