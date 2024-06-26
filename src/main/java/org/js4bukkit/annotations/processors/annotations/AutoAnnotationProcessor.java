package org.js4bukkit.annotations.processors.annotations;

import org.js4bukkit.annotations.processors.interfaces.AnnotatedClassProcessorInterface;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于标记需要自动处理注解的具体实现类。
 *
 * <p>
 * 使用该注解的类必须实现 {@link AnnotatedClassProcessorInterface} 接口。
 * </p>
 *
 * @author NaerQAQ
 * @version 1.0
 * @see AnnotatedClassProcessorInterface
 * @since 2023/12/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoAnnotationProcessor {
    /**
     * 需要处理的注解类对象。
     *
     * @return 需要处理的注解类对象
     */
    Class<? extends Annotation> annotationClass();
}
