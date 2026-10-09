/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import net.minecraft.world.InteractionResult;

/**
 * Interaction results shared by item, block and entity interactions in Minecraft 26.x.
 *
 * <p>{@link InteractionResult#SUCCESS} consumes the action and selects a client-originated swing;
 * {@link InteractionResult#SUCCESS_SERVER} selects a server-originated swing. {@link InteractionResult#CONSUME}
 * consumes without a swing. Preserve the old sided-success behavior with client SUCCESS and server CONSUME.
 *
 * <p>{@link InteractionResult#TRY_WITH_EMPTY_HAND} requests the block's empty-hand interaction.
 * {@link InteractionResult#PASS} passes without requesting that fallback; these are not interchangeable.
 * {@link InteractionResult#FAIL} rejects the interaction.
 *
 * <p>Successful item use can replace the held stack using
 * {@link InteractionResult.Success#heldItemTransformedTo(net.minecraft.world.item.ItemStack)}.
 * Use {@link InteractionResult.Success#withoutItem()} for successes that should not count as item use.
 * Preserve this context when forwarding a result between event handlers instead of reducing it to a constant.
 */
@SuppressWarnings("unused")
public interface DocumentedInteractionResult {}
