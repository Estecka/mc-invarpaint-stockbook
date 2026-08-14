package tk.estecka.invarpaint.stockbook.mixin;

import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@Mixin(Items.class)
public interface IItemsMixin
{
	@Invoker
	static public Item callRegisterItem(ResourceKey<Item> key, Function<Item.Properties, Item> itemFactory, Item.Properties properties){
		throw new AssertionError();
	}
}
