package com.omricat.maplibrarian.workflow.assertk

import assertk.Assert
import com.squareup.workflow1.WorkflowOutput

internal fun <T> Assert<WorkflowOutput<T>>.value(): Assert<T> =
    transform(name = "value") { it.value }
