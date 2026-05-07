package com.example.sessioncodec;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;

@ImportRuntimeHints(KryoSessionRuntimeHints.Registrar.class)
public final class KryoSessionRuntimeHints {

    private KryoSessionRuntimeHints() {
    }

    public static final class Registrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(SessionUser.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(SessionUserKryoSerializer.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerType(InstantKryoSerializer.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerType(DurationKryoSerializer.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerType(KryoRedisSerializer.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerType(KryoFactory.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        }
    }
}
