@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins { alias(libs.plugins.maplib.kotlin.library) }

dependencies { implementation(projects.util.logging) }
