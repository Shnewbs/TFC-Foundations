# EUPL-1.2. Source-level regression guards for the Minecraft 26.1 removal port.
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2] / "src/main/java/net/dries007/tfc"

def source(relative):
    return (ROOT / relative).read_text(encoding="utf-8")

class RemovalAndRabbitMigrationGuards(unittest.TestCase):
    def test_device_inventory_removed_before_entity_disappears(self):
        device = source("common/blocks/devices/DeviceBlock.java")
        inventory = source("common/blockentities/InventoryBlockEntity.java")
        self.assertIn("handleInventoryRemoval(InventoryBlockEntity<?> entity)", device)
        self.assertIn("public void preRemoveSideEffects(BlockPos pos, BlockState state)", inventory)
        self.assertIn("block.handleInventoryRemoval(this)", inventory)
        self.assertNotIn("protected void onRemove(", device)
        self.assertNotIn("affectNeighborsAfterRemoval(", device)
        self.assertIn("clearContent();", inventory)

    def test_ingot_drops_before_removal(self):
        block = source("common/blocks/devices/IngotPileBlock.java")
        entity = source("common/blockentities/IngotPileBlockEntity.java")
        self.assertNotIn("protected void onRemove(", block)
        self.assertIn("public void preRemoveSideEffects(BlockPos pos, BlockState state)", entity)
        self.assertIn("removeAllIngots(stack -> Block.popResource(level, pos, stack))", entity)
        self.assertIn("pile.removeAllIngots(ingot -> {})", block)

    def test_rabbit_uses_synced_tfc_variant_with_legacy_genes(self):
        rabbit = source("common/entities/prey/TFCRabbit.java")
        self.assertIn("private static final EntityDataAccessor<Integer> TFC_VARIANT", rabbit)
        self.assertIn("builder.define(TFC_VARIANT, 0)", rabbit)
        self.assertIn("return Variant.byId(entityData.get(TFC_VARIANT))", rabbit)
        self.assertIn('nbt.putInt("TFCVariant", getVariant().id())', rabbit)
        self.assertIn('nbt.getIntOr("TFCVariant", super.getVariant().id())', rabbit)
        self.assertIn("readGeneVariant(CompoundTag tag, String key)", rabbit)

if __name__ == "__main__":
    unittest.main()
