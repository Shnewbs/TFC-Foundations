/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import net.dries007.tfc.common.capabilities.transfer.TransferJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real NeoForge transactions and the production storage journal, with independently modeled scalar stores. */
public final class TransferJournalSmoke
{
    private static int assertions;
    private static void check(boolean value) { assertions++; if (!value) throw new AssertionError("Transfer assertion " + assertions); }
    private static final class Store
    {
        int value;
        int notifications;
        int snapshots;
        final TransferJournal<Integer> journal = new TransferJournal<>(() -> { snapshots++; return value; }, saved -> value = saved,
            saved -> notifications++, () -> notifications++);
        void add(int amount) { journal.beforeChange(); value += amount; journal.changed(); }
    }

    private static void combinations()
    {
        for (int seed = 0; seed < 128; seed++)
        for (int mask = 0; mask < 16; mask++)
        {
            final Store items = new Store();
            final Store fluid = new Store();
            items.value = seed; fluid.value = seed * 3;
            final boolean rootCommit = (mask & 1) != 0, childCommit = (mask & 2) != 0;
            final boolean grandchildCommit = (mask & 4) != 0, beforeChild = (mask & 8) != 0;
            try (var root = Transaction.openRoot())
            {
                TransferJournal.requireCurrent(root);
                if (beforeChild) { items.add(1); fluid.add(2); }
                try (var child = TransferJournal.openLegacyTransaction())
                {
                    boolean rejected = false;
                    try { TransferJournal.requireCurrent(root); } catch (IllegalStateException expected) { rejected = true; }
                    check(rejected);
                    items.add(3); fluid.add(5);
                    try (var grandchild = TransferJournal.openLegacyTransaction())
                    {
                        items.add(7); fluid.add(11);
                        if (grandchildCommit) grandchild.commit();
                    }
                    check(items.value == seed + (beforeChild ? 1 : 0) + 3 + (grandchildCommit ? 7 : 0));
                    check(fluid.value == seed * 3 + (beforeChild ? 2 : 0) + 5 + (grandchildCommit ? 11 : 0));
                    if (childCommit) child.commit();
                }
                check(items.value == seed + (beforeChild ? 1 : 0) + (childCommit ? 3 + (grandchildCommit ? 7 : 0) : 0));
                check(fluid.value == seed * 3 + (beforeChild ? 2 : 0) + (childCommit ? 5 + (grandchildCommit ? 11 : 0) : 0));
                check(items.notifications == 0 && fluid.notifications == 0);
                items.add(13); fluid.add(17);
                if (rootCommit) root.commit();
            }
            check(items.value == seed + (rootCommit ? (beforeChild ? 1 : 0) + (childCommit ? 3 + (grandchildCommit ? 7 : 0) : 0) + 13 : 0));
            check(fluid.value == seed * 3 + (rootCommit ? (beforeChild ? 2 : 0) + (childCommit ? 5 + (grandchildCommit ? 11 : 0) : 0) + 17 : 0));
            check(items.notifications == (rootCommit ? 1 : 0) && fluid.notifications == (rootCommit ? 1 : 0));
            check(!items.journal.isInTransaction() && !fluid.journal.isInTransaction());
            final int before = items.value;
            try (var simulation = TransferJournal.openLegacyTransaction()) { items.add(97); }
            check(items.value == before);
            items.add(1);
            check(items.notifications == (rootCommit ? 2 : 1));
        }
    }

    private static void failuresAndCallbacks()
    {
        final Store store = new Store();
        try (var root = Transaction.openRoot())
        {
            store.journal.beforeChange(); // no mutation: no callback
            root.commit();
        }
        check(store.notifications == 0);
        try
        {
            try (var root = Transaction.openRoot()) { store.add(10); throw new IllegalArgumentException("fixture"); }
        }
        catch (IllegalArgumentException expected) { check(store.value == 0 && store.notifications == 0); }
        try (var root = Transaction.openRoot())
        {
            try (var child = TransferJournal.openLegacyTransaction()) { store.add(1); }
            root.commit();
        }
        check(store.value == 0 && store.notifications == 0);
        final int[] value = {0}, calls = {0};
        @SuppressWarnings("unchecked") final TransferJournal<Integer>[] journal = new TransferJournal[1];
        journal[0] = new TransferJournal<>(() -> value[0], saved -> value[0] = saved, saved -> {
            calls[0]++;
            if (calls[0] == 1)
            {
                try (var followup = TransferJournal.openLegacyTransaction())
                {
                    journal[0].beforeChange(); value[0]++; journal[0].changed(); followup.commit();
                }
            }
        }, () -> calls[0]++);
        try (var root = Transaction.openRoot()) { journal[0].beforeChange(); value[0]++; journal[0].changed(); root.commit(); }
        check(value[0] == 2 && calls[0] == 2 && !journal[0].isInTransaction());
    }

    public static void main(String[] args)
    {
        combinations(); failuresAndCallbacks();
        System.out.println("PASS " + assertions + " production journal/native transaction assertions (2,048 coupled-store scenarios, nested commit/abort, simulation, exception rollback, callback deferral/reentrancy).");
        System.out.println("NOT RUN: game-backed item/fluid adapters, coupled container components, world automation or transformed Mixins.");
    }
}
