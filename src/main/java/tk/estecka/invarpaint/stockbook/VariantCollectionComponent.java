package tk.estecka.invarpaint.stockbook;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.component.DataComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class VariantCollectionComponent
{
	static public final Identifier ID = new Identifier("invarpaint", "stockbook_content");
	static public final Codec<VariantCollectionComponent> CODEC = Codec.unboundedMap(Identifier.CODEC, Codecs.NONNEGATIVE_INT).xmap(VariantCollectionComponent::new, v->v.content);
	static public final DataComponentType<VariantCollectionComponent> TYPE = DataComponentType.<VariantCollectionComponent>builder().codec(CODEC).build();

	public final Map<@NotNull Identifier, @NotNull Integer> content;

	static public void Register(){
		Registry.register(Registries.DATA_COMPONENT_TYPE, ID, TYPE);
	}

	public VariantCollectionComponent(Map<@NotNull Identifier, @NotNull Integer> map){
		this.content = ImmutableMap.copyOf(map);
		this.Validate();
	}

	public void Validate(){
		for (var e : this.content.entrySet())
			assert e.getKey()!=null && e.getValue()!=null;
	}
}
