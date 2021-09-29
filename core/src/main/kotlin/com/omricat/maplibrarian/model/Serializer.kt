package com.omricat.maplibrarian.model

public interface Serializer<in ModelT> {
    public operator fun invoke(mapModel: ModelT): Map<String, Any?>
}
