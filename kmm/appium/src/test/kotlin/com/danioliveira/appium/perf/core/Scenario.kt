package com.danioliveira.appium.perf.core

/**
 * Represents a multi-flow performance scenario with multiple iterations.
 *
 * @property name Scenario name for reporting
 * @property iterations Number of times to run the entire scenario (default: 3)
 * @property flows List of flows to execute sequentially in each iteration (can be Flow or FlowWithActions)
 */
data class Scenario(
    val name: String,
    val iterations: Int = 3,
    val flows: List<Any> // Can be Flow or FlowWithActions
) {
    /**
     * Get regular flows (without action tracking).
     */
    fun filterFlows(): List<Flow> = flows.filterIsInstance<Flow>()
    
    /**
     * Get flows with action tracking.
     */
    fun filterFlowsWithActions(): List<FlowWithActions> = flows.filterIsInstance<FlowWithActions>()
}

/**
 * Represents a single flow within a scenario.
 *
 * @property name Flow name for reporting
 * @property steps The actions to perform in this flow
 * @property expectedScreens List of screen names expected to appear (for segmentation)
 * @property durationMs Optional duration hint for this flow
 */
data class Flow(
    val name: String,
    val steps: suspend () -> Unit,
    val expectedScreens: List<String> = emptyList(),
    val durationMs: Long? = null
)

/**
 * Represents a flow with explicit action tracking.
 * Each action will have before/after ADB snapshots captured automatically.
 *
 * @property name Flow name for reporting
 * @property actions List of named actions to execute with metric tracking
 * @property expectedScreens List of screen names expected to appear (for segmentation)
 * @property durationMs Optional duration hint for this flow
 */
data class FlowWithActions(
    val name: String,
    val actions: List<Action>,
    val expectedScreens: List<String> = emptyList(),
    val durationMs: Long? = null
)

/**
 * Represents a single action within a flow.
 * The framework will automatically capture before/after ADB snapshots.
 *
 * @property name Action name for reporting (e.g., "EnterEmail", "ClickLogin")
 * @property execute The action to perform
 */
data class Action(
    val name: String,
    val execute: suspend () -> Unit
)


