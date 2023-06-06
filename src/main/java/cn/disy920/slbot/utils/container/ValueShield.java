package cn.disy920.slbot.utils.container;

public final class ValueShield<T, R> {

    private final T instance;

    public ValueShield(T instance) {
        this.instance = instance;
    }

    public R runOrReturn(OneValueFunction<T, R> func, R def) {
        if (instance != null) {
            return func.run(instance);
        }
        else {
            return def;
        }
    }

    @FunctionalInterface
    public interface OneValueFunction<T, R> {
        R run(T value);
    }
}
