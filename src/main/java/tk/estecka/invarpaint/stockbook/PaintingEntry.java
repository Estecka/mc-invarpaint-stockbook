package tk.estecka.invarpaint.stockbook;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

/**
 * Just  some  wrapper  for {@code Holder<PaintingVariant>}, to  ease  having to
 *carry around that cumbersome class name in every function parameter and return
 * types.
 */
public class PaintingEntry
{
	static public final Codec<PaintingEntry> CODEC = PaintingVariant.CODEC.xmap(PaintingEntry::new, PaintingEntry::entry);

	public final Holder<PaintingVariant> entry;
	public final PaintingVariant value;
	public final Identifier id;

	public PaintingEntry(@NotNull Holder<PaintingVariant> entry){
		this.entry = entry;
		this.value = entry.value();
		this.id = entry.unwrapKey().get().identifier();
	}

	public Holder<PaintingVariant> entry(){ return this.entry; }
	public PaintingVariant value(){ return this.value; }
	public Identifier id(){ return this.id; }

	@Override
	public boolean equals(Object other) {
		if (other instanceof PaintingEntry entry)
			return this.id.equals(entry.id);
		else
			return super.equals(other);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return entry.getRegisteredName();
	}
}
