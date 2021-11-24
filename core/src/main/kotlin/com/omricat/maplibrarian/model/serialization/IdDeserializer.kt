package com.omricat.maplibrarian.model.serialization

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.toResultOr
import com.omricat.maplibrarian.model.DbChartModelDeserializer.Error

public interface IdDeserializer<ValueT, ErrorT> {
    public operator fun invoke(
        id: String,
        properties: Map<String, Any?>
    ): Result<ValueT, ErrorT>
}

public inline fun <reified T> Map<String, Any?>.getProperty(propertyName: String):
    Result<T, Error> =
    get(propertyName).toResultOr { Error("Property $propertyName not found in $this") }
        .flatMap {
            (it as? T).toResultOr {
                Error(
                    "Can't cast property $propertyName ($it : ${it::class}) " +
                        "to type ${T::class.simpleName}"
                )
            }
        }
