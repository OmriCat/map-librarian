package com.omricat.maplibrarian.model.serialization

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.toResultOr
import com.omricat.maplibrarian.model.serialization.DeserializerError.CastError
import com.omricat.maplibrarian.model.serialization.DeserializerError.PropertyNonFoundError
import kotlin.reflect.KClass

public interface FromMapWithIdDeserializer<ValueT, ErrorT : DeserializerError> {
    public operator fun invoke(
        id: String,
        properties: Map<String, Any?>
    ): Result<ValueT, ErrorT>
}

public interface DeserializerError {

    public val message: String

    public data class CastError(val sourceClass: KClass<*>, val targetClass: KClass<*>) :
        DeserializerError {
        override val message: String
            get() = "Can't cast instance of $sourceClass to $targetClass"
    }

    public data class PropertyNonFoundError(val propertyName: String, val map: Map<String, Any?>) :
        DeserializerError {
        override val message: String
            get() = "Property $propertyName not found in $map"
    }
}

public inline fun <reified T> Map<String, Any?>.getProperty(propertyName: String):
    Result<T, DeserializerError> =
    get(propertyName).toResultOr { PropertyNonFoundError(propertyName, this) }
        .flatMap {
            (it as? T).toResultOr {
                CastError(sourceClass = it::class, targetClass = T::class)
            }
        }

public interface ToMapSerializer<in ModelT> {
    public operator fun invoke(model: ModelT): Map<String, Any?>
}
