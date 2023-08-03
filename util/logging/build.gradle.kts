@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins { alias(libs.plugins.maplib.kotlin.library) }

dependencies { api(libs.kermit.core) }

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xcontext-receivers") } }
