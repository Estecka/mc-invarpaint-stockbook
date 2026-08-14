package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class VariableSizeInventory
implements Container
{
	private final List<ItemStack> content =  new ArrayList<>();

	public VariableSizeInventory(){
	}

	@Override
	public int getContainerSize(){
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
	public ItemStack getItem(int i){
		return i < content.size() ? content.get(i) : ItemStack.EMPTY;
	}

	@Override
	public void setItem(int i, ItemStack stack){
		while (content.size() <= i)
			this.content.add(ItemStack.EMPTY);

		this.content.set(i, stack);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i){
		ItemStack r = ItemStack.EMPTY;
		if (i < content.size()) {
			r = content.get(i);
			content.set(i, ItemStack.EMPTY);
		}
		return r;
	}

	@Override
	public ItemStack removeItem(int i, int amount){
		ItemStack r = ItemStack.EMPTY;

		if (i < content.size()) {
			ItemStack original = content.get(i);
			r = original.copyWithCount(amount);
			original.shrink(amount);
			content.set(i, original);
		}

		return r;
	}

	@Override
	public void clearContent(){
		this.content.clear();
	}

	@Override
	public void setChanged(){
	}

	@Override
	public boolean stillValid(Player player){
		return true;
	}
}
