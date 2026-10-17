/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Compatibility boundary for legacy mutable storage participating in native transactions. */
public final class TransferJournal<T> extends SnapshotJournal<TransferJournal.State<T>>
{
    public record State<T>(T value, boolean changed) {}
    private final Supplier<T> snapshot;
    private final Consumer<T> restore;
    private final Consumer<T> commit;
    private final Runnable immediate;
    private boolean changed;

    public TransferJournal(Supplier<T> snapshot, Consumer<T> restore, Consumer<T> commit, Runnable immediate)
    {
        this.snapshot = snapshot;
        this.restore = restore;
        this.commit = commit;
        this.immediate = immediate;
    }

    /** Call before a mutable operation, including changes to components of a stored stack. */
    @SuppressWarnings("deprecation")
    public void beforeChange()
    {
        final var transaction = Transaction.getCurrentOpenedTransaction();
        if (transaction != null) updateSnapshots(transaction);
    }

    /** Notify only after mutation; irreversible callbacks wait until the root commits. */
    public void changed()
    {
        if (isInTransaction()) changed = true;
        else immediate.run();
    }

    @Override protected State<T> createSnapshot() { return new State<>(snapshot.get(), changed); }
    @Override protected void revertToSnapshot(State<T> state) { restore.accept(state.value()); changed = state.changed(); }
    @Override protected void onRootCommit(State<T> original)
    {
        final boolean notify = changed;
        changed = false;
        if (notify) commit.accept(original.value());
    }

    @SuppressWarnings("deprecation")
    public static Transaction openLegacyTransaction()
    {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }

    @SuppressWarnings("deprecation")
    public static void requireCurrent(TransactionContext transaction)
    {
        if (Transaction.getCurrentOpenedTransaction() != transaction)
            throw new IllegalStateException("Transfer must use the currently open transaction");
    }
}
