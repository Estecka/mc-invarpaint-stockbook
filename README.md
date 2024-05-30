# Invariable Paintings: Collector's Stockbook

## Overview
An addon for [**Invariable Paintings**](https://modrinth.com/mod/invariable-paintings).

This adds a new item to help declutter the inventory: the Stockbook. A portable conainer somewhat similar to a bundle, but strictly limited to painting items, and with some added functionality.

This addon requires Invariable Paintings to be installed on both client and server.

## Functionalities
### Storage
Use the item in either hand in order to open its inventory screen.

The stockbook's inventory can hold up to one full stack of every painting variant. Stored paintings are always sorted by their variant id.

### Preview
Hovering a painting item with the mouse cursor shows what the actual painting looks like when hanged.

### Search
Paintings can be filtered based on **title, size, id** and **author** using the search bar at the top of the inventory. `Enter` and `Esc` can be used to quickly select and deselect the search bar without using the mouse.

### Placeholders
The container keeps track of the paintings that have been discovered. 
Even after a painting is removed, it will leave a placeholder behind.

Both the number of painting stored and discovered are displayed on the sotckbook's tooltip.


## Caveats
When stored into the stockbook, painting items are reduced to nothing but their variant. Any other custom data (like names) will be discarded.
