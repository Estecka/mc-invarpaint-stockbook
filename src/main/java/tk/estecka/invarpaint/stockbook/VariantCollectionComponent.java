package tk.estecka.invarpaint.stockbook;

import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;

public class VariantCollectionComponent
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath("invarpaint", "stockbook_content");
	static public final Codec<VariantCollectionComponent> CODEC = Codec.unboundedMap(PaintingEntry.CODEC, ExtraCodecs.NON_NEGATIVE_INT).xmap(VariantCollectionComponent::new, v->v.content);
	static public final DataComponentType<VariantCollectionComponent> TYPE = DataComponentType.<VariantCollectionComponent>builder().persistent(CODEC).build();

	public final Map<@NotNull PaintingEntry, @NotNull Integer> content;

	static public void Register(){
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ID, TYPE);
	}

	public VariantCollectionComponent(Map<@NotNull PaintingEntry, @NotNull Integer> map){
		this.content = ImmutableMap.copyOf(map);
		this.Validate();
	}

	public void Validate(){
		for (var e : this.content.entrySet()) {
			Objects.requireNonNull(e.getKey());
			Objects.requireNonNull(e.getValue());
		}
	}

	@Override
	public boolean equals(Object other){
		return other instanceof VariantCollectionComponent collection && collection.content.equals(this.content);
	}
}
