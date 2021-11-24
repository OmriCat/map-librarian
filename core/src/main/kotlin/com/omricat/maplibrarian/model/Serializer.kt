package com.omricat.maplibrarian.model

public interface Serializer<in ModelT> {
    public operator fun invoke(model: ModelT): Map<String, Any?>
}
