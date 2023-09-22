package io.github.goldfish07.reschiper.plugin.internal;

import com.android.build.gradle.api.ApplicationVariant;
import io.github.goldfish07.reschiper.plugin.model.KeyStore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SigningConfig {
    @Contract("_ -> new")
    public static @NotNull KeyStore getSigningConfig(@NotNull ApplicationVariant variant) {
        return new KeyStore(
                variant.getSigningConfig().getStoreFile(),
                variant.getSigningConfig().getStorePassword(),
                variant.getSigningConfig().getKeyAlias(),
                variant.getSigningConfig().getKeyPassword()
        );
    }
}
