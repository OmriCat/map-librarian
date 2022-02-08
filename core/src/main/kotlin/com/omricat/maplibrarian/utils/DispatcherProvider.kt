package com.omricat.maplibrarian.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public interface DispatcherProvider {

    public val default: CoroutineDispatcher
    public val io: CoroutineDispatcher
    public val main: CoroutineDispatcher
    public val unconfined: CoroutineDispatcher

    public companion object Default : DefaultDispatcherProvider()

    public abstract class DefaultDispatcherProvider : DispatcherProvider {
        override val default: CoroutineDispatcher get() = Dispatchers.Default
        override val io: CoroutineDispatcher get() = Dispatchers.IO
        override val main: CoroutineDispatcher get() = Dispatchers.Main
        override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
    }
}
