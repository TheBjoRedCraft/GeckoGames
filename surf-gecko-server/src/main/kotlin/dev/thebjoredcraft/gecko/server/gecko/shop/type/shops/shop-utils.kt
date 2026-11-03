package dev.thebjoredcraft.gecko.server.gecko.shop.type.shops

import dev.thebjoredcraft.gecko.server.gecko.shop.ShopItem
import dev.thebjoredcraft.gecko.server.gecko.util.removeItalics
import dev.thebjoredcraft.gecko.server.i18n.language
import dev.thebjoredcraft.gecko.server.i18n.translate
import dev.thebjoredcraft.gecko.server.i18n.translateLines
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

fun buildShopItemDisplay(shopItem: ShopItem, viewer: Player): ItemStack =
    shopItem.inventoryItem(viewer.language).builder()
        .set(
            DataComponents.ITEM_NAME,
            viewer.translate("shop.item.display.name", "name" to shopItem.displayName)
        )
        .set(
            DataComponents.LORE,
            viewer.translateLines(
                "shop.item.display.lore",
                "description" to shopItem.description,
                "price" to shopItem.price
            ).removeItalics()
        )
        .build()
