package tk.estecka.invarpaint.stockbook.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface ISlotMixin
{
	@Mutable @Accessor void setX(int x);
	@Mutable @Accessor void setY(int y);
}
