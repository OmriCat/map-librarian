package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.toResultOr
import com.omricat.maplibrarian.chartlist.DeserializerError.CastError
import com.omricat.maplibrarian.chartlist.DeserializerError.PropertyNonFoundError
import com.omricat.maplibrarian.chartlist.MapModelProperties.TITLE
import com.omricat.maplibrarian.model.ChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import kotlin.reflect.KClass

private object MapModelProperties {

    const val TITLE = "title"
}

public object ChartModelToMapSerializer {
    fun serializeToMap(model: ChartModel): Map<String, String> =
        hashMapOf(
            TITLE to model.title,
        )
}

public object ChartModelFromMapDeserializer {
    fun deserializeFromMap(properties: Map<String, Any?>): Result<ChartModel, DeserializerError> =
        binding {
            val title = properties.getProperty<String>(TITLE).bind()
            UnsavedChartModel(title = title)
        }
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

private inline fun <reified T> Map<String, Any?>.getProperty(
    propertyName: String
): Result<T, DeserializerError> =
    get(propertyName)
        .toResultOr { PropertyNonFoundError(propertyName, this) }
        .flatMap {
            (it as? T).toResultOr { CastError(sourceClass = it::class, targetClass = T::class) }
        }
