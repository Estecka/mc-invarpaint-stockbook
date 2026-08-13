# Difference between versions
## 1.19.4
Initial development

## 1.20.5
Initial Release

## 1.21.0
### Breaking:
- The painting registry can no longer be accessed statically.
- `PaintingVariant` now measures its size in blocks instead of pixels.
### Mappings:
- `DataComponentType` was renamed to ComponentType.
- `TooltipType` was moved to another package.

## 1.21.2
### Breaking:
- `TypedActionResult` was removed in favor of `ActionResult`
- All variants of "draw" in `DrawContext` now require a `RenderLayer` in some form as argument.
- The order of existing arguments in `DrawContext.drawTexture` was rearranged.
- The order of existing arguments in `DrawContext.drawSprite` was rearranged.
- `Item`'s are now super intrusive and can't be instantiated without first being registered. You can register a factory for the item instead.
### Mappings:
- `Codecs.NONNEGATIVE_INT` renamed to `NON_NEGATIVE_INT`
- `WrapperLookup.getOptionalWrapper` renamed to `getOptional`
- `DynamicRegistryManager.get` renamed to `getOrThrow`
- `Registry.getOrEmpty` renamed to `getOptionalValue`
- `DrawContext.drawSprite` renamed to `drawSpriteStretched`.

## 1.21.5
- Painting Variants are now stored as regitry entries instead of identifiers.
- Item Stack variants are no longer stored in the `entity_data` component.

## 1.21.6
- `DrawContext::drawGuiTexture` now takes a render pipeline instead of a render layer.
- `DrawContext::getMatrices` now returns a standard 3x2 matrice instead of Mojang's own 4x4.
- `DrawContext::drawTooltip`'s now takes an extra boolean parameter.

## 1.21.9
- `Entity::getWorld` was replaced with `HeldItemContext::getEntityWorld`.
- UI methods now take `Click`, `KeyInput`, or `CharInput` instead of raw data: `Element::onMouseClicked`,`onMouseReleased`,`mouseDragged`,`charTyped`.
- `MinecraftClient::getPaintingManager` was replaced with `getAtlasManager`.

## 1.21.11
- `ToggleButtonWidget` was removed. Use `CyclingButtonWidget::onOffBuilder` instead.
