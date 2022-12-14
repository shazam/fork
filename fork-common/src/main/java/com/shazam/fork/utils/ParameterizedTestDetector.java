package com.shazam.fork.utils;

import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.dexbacked.value.DexBackedTypeEncodedValue;
import org.jf.dexlib2.iface.ClassDef;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ParameterizedTestDetector {
    public static final String RUN_WITH_ANNOTATION = "Lorg/junit/runner/RunWith;";
    public static final Set<String> PARAMETRIZED_ANNOTATIONS = new HashSet<>(Arrays.asList(
            "Lorg/junit/runners/Parameterized;",
            "Lio/qameta/allure/kotlin/junit4/AllureParametrizedRunner;"
    ));

    public static boolean isParameterizedClass(ClassDef classDef) {
        Optional<DexBackedTypeEncodedValue> encodedValue = classDef.getAnnotations().stream()
                .filter(it -> RUN_WITH_ANNOTATION.equals(it.getType()))
                .flatMap(it -> it.getElements().stream())
                .filter(it ->
                        it.getValue().getValueType() == ValueType.TYPE
                        && it.getValue() instanceof DexBackedTypeEncodedValue)
                .findFirst()
                .map(it -> (DexBackedTypeEncodedValue) it.getValue());
        return encodedValue.isPresent()
                && PARAMETRIZED_ANNOTATIONS.contains(encodedValue.get().getValue());
    }
}
