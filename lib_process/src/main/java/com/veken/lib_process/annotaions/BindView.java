package com.veken.lib_process.annotaions;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Veken on 2023/10/30 15:57
 *
 * @desc
 */

@Retention(RUNTIME) @Target(FIELD)
public @interface BindView {
    @IdRes int value();
}
