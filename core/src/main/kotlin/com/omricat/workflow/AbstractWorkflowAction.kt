package com.omricat.workflow

import com.squareup.workflow1.WorkflowAction

/**
 * Convenience class to make aid with defining actions as sealed classes.
 *
 * To use it, define an open class extending from it as a nested class of the Workflow.
 */
public abstract class AbstractWorkflowAction<PropsT, StateT, OutputT>(
    private val name: () -> String,
    private val updater: WorkflowAction<PropsT, StateT, OutputT>.Updater.() -> Unit
) : WorkflowAction<PropsT, StateT, OutputT>() {
    override fun Updater.apply(): Unit = updater.invoke(this)

    override fun toString(): String = "WorkflowAction(${name()})@${hashCode()}"
}
