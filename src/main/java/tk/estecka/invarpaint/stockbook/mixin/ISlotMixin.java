package tk.estecka.invarpaint.stockbook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public interface ISlotMixin
{
	@Mutable @Accessor void setX(int x);
	@Mutable @Accessor void setY(int y);
}
