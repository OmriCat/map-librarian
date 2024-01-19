plugins { alias(libs.plugins.maplib.kotlin.library) }

dependencies { api(libs.kermit.core) }

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xcontext-receivers") } }
