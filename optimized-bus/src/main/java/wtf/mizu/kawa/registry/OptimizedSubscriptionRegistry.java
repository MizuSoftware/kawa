package wtf.mizu.kawa.registry;

import org.jetbrains.annotations.NotNull;
import wtf.mizu.kawa.api.Subscription;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An optimized {@link SubscriptionRegistry}. Turns into a
 * {@link SingletonSubscriptionRegistry} if only one item is left after
 * removal, or into an {@link EmptySubscriptionRegistry} if no item is left ;
 * meaning if a {@link SubscriptionRegistry} object is instance of
 * {@link OptimizedSubscriptionRegistry}, it inevitably has at least 2 items.
 *
 * @param <T> the topic.
 * @author Shyrogan
 * @author lambdagg
 * @since 0.0.1
 */
public class OptimizedSubscriptionRegistry<T>
        implements SubscriptionRegistry<T> {
    private final @NotNull List<Subscription<T>> subscriptions;
    private final Consumer<T>[] consumers;

    @SuppressWarnings("unchecked")
    public OptimizedSubscriptionRegistry(
            final @NotNull List<Subscription<T>> subscriptions
    ) {
        // TODO maybe we want to synchronize stuff here?
        this.subscriptions = subscriptions;
        this.consumers = (Consumer<T>[]) subscriptions.stream()
                .map(Subscription::consumer)
                .toArray(Consumer[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<Subscription<T>> subscriptions() {
        return Collections.unmodifiableList(this.subscriptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull SubscriptionRegistry<T> add(
            final @NotNull Subscription<T> subscription
    ) {
        if (!this.subscriptions.contains(subscription)) {
            this.subscriptions.add(subscription);
            Collections.sort(this.subscriptions);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull SubscriptionRegistry<T> remove(
            final @NotNull Subscription<T> subscription
    ) {
        this.subscriptions.remove(subscription);

        return this.subscriptions.size() != 1 ?
                this :
                new SingletonSubscriptionRegistry<>(
                        this.subscriptions.get(0)
                );
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public void publish(final @NotNull T event) {
        for (int i = 0; i < consumers.length; i++) {
            consumers[i].accept(event);
        }
    }
}
