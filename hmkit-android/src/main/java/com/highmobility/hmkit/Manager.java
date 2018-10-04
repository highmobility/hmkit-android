package com.highmobility.hmkit;

/**
 * @deprecated use {@link HMKit} instead.
 */
@Deprecated
public class Manager {
    /**
     * @return The HMKit instance.
     * @deprecated use {@link HMKit#getInstance()} instead.
     */
    @Deprecated
    public static HMKit getInstance() {
        return HMKit.getInstance();
    }

    private Manager() {}
}
