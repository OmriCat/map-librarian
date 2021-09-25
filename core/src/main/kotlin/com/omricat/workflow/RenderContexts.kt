package com.omricat.workflow

import com.squareup.workflow1.BaseRenderContext
import com.squareup.workflow1.WorkflowAction

public fun <P, S, O> BaseRenderContext<P, S, O>.eventHandler(
    action: WorkflowAction<P, S, O>
): () -> Unit = { actionSink.send(action) }

public inline fun <P, S, O> BaseRenderContext<P, S, O>.eventHandler(
    crossinline actionProducer: () -> WorkflowAction<P, S, O>
): () -> Unit = { actionSink.send(actionProducer()) }

public inline fun <P, S, O, EventT> BaseRenderContext<P, S, O>.eventHandler(
    crossinline actionProducer: (EventT) -> WorkflowAction<P, S, O>
): (EventT) -> Unit = { e -> actionSink.send(actionProducer(e)) }

public inline fun <P, S, O, E1, E2> BaseRenderContext<P, S, O>.eventHandler(
    crossinline actionProducer: (E1, E2) -> WorkflowAction<P, S, O>
): (E1, E2) -> Unit = { e1, e2 -> actionSink.send(actionProducer(e1, e2)) }
