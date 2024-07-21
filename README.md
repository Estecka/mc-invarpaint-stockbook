# Invariable Paintings: Collector's Stockbook

## Overview
An addon for [**Invariable Paintings**](https://modrinth.com/mod/invariable-paintings).
Both mods must be installed on both client and server.

This adds a new item to help declutter the inventory: the Stockbook. A portable container, similar to a bundle in spirit, but strictly limited to painting items, and no limit on how many variants it can hold.


## Functionalities
### Storage
Use the item in either hand in order to open its inventory screen.

The stockbook's inventory can hold up to one full stack of every painting variant. Stored paintings are always sorted by their variant ID.

The stockbook itself can be stored into a chiseled bookshelf.

### Preview
Hovering a painting item with the mouse cursor shows what the actual painting looks like when hung up.

### Search
Paintings can be filtered based on **title, size, id** and **author**.
The keys `Enter` and `Esc` can be used to quickly select and deselect the search bar without using the mouse.

### Placeholders
The stockbook keeps track of the paintings that have been discovered.
Even after a painting is removed, it will leave a placeholder behind.
Placeholders can be hidden using the button near the search bar.

Both the number of paintings stored and discovered are displayed on the stockbook's tooltip.


## Caveats
When stored into the stockbook, painting items are reduced to nothing but their variants. Any other custom data (like names) will be discarded. This is a similar behaviour to paintings being hung to then removed from a wall.
