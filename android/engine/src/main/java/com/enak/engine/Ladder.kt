package com.enak.engine

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class Step(val raw: String, val displayName: String) {
    NUDGE("nudge", "Gentle nudge"),
    REFLECT("reflect", "Reflection prompt"),
    FRICTION("friction", "Soft friction"),
    REDIRECT("redirect", "Redirect"),
    EXTEND("extend", "Conscious extension")
}

enum class Personality {
    CALM,
    FUNNY,
    NERD,
    STRICT;

    override fun toString(): String = name.lowercase()
}

data class LadderInput(
    val overrun: OverrunState,
    val label: Label,
    val exitLoop: Boolean,
    /** highest step already shown this session, -1 if none */
    val shown: Int,
    val sleepGuard: Boolean,
    val gentleMode: Boolean
)

data class PromptContext(
    val purpose: Purpose,
    val elapsedMin: Int,
    val plannedMin: Int,
    val matched: Int,
    val goal: String? = null
)

object Ladder {
    val LADDER = listOf(Step.NUDGE, Step.REFLECT, Step.FRICTION, Step.REDIRECT, Step.EXTEND)

    fun nextStep(input: LadderInput): Step? {
        if (input.label == Label.FOCUSED && !input.exitLoop) return null
        var target = -1
        if (input.overrun == OverrunState.NEARING) target = 0
        if (input.overrun == OverrunState.OVER) target = if (input.label == Label.COMPULSIVE) 2 else 1
        if (input.overrun == OverrunState.FAR_OVER) target = if (input.label == Label.COMPULSIVE) 3 else 2
        if (input.exitLoop) target = max(target, 1)
        if (input.overrun == OverrunState.ON_TRACK && input.label == Label.COMPULSIVE) target = max(target, 0)
        if (input.sleepGuard && target >= 0) target = min(target + 1, 3)
        if (input.gentleMode) target = min(target, 1)
        if (target < 0 || target <= input.shown) return null
        return LADDER.getOrNull(target)
    }

    private fun purposeWord(p: Purpose): String = when (p) {
        Purpose.LEARN -> "learn"
        Purpose.RELAX -> "relax"
        Purpose.SOCIAL -> "catch up with people"
        Purpose.RESEARCH -> "research something"
        Purpose.QUICK -> "do a quick check"
    }

    fun promptCopy(step: Step, p: Personality, ctx: PromptContext): String {
        val came = "You came to ${purposeWord(ctx.purpose)}"
        val t = "${ctx.elapsedMin} of ${ctx.plannedMin} min"
        val pct = if (ctx.plannedMin > 0) ((ctx.elapsedMin.toDouble() / ctx.plannedMin.toDouble()) * 100.0).roundToInt() else 100

        return when (p) {
            Personality.CALM -> when (step) {
                Step.NUDGE -> "$t. No rush — just a heads-up."
                Step.REFLECT -> "$came. ${if (!ctx.goal.isNullOrBlank()) "Did you find \"${ctx.goal}\"?" else "Found what you were looking for?"}"
                Step.FRICTION -> "Let's breathe for a moment. Why would you like to keep going?"
                Step.REDIRECT -> "Maybe a glass of water or a short walk would feel good now."
                Step.EXTEND -> "You can take 5 more minutes — just say why."
            }
            Personality.FUNNY -> when (step) {
                Step.NUDGE -> "$t. Your thumb has run a small marathon."
                Step.REFLECT -> "$came. ${ctx.matched} useful things so far. Victory lap or exit?"
                Step.FRICTION -> "Plot twist: the feed never ends. You do. Why continue?"
                Step.REDIRECT -> "Fridge, window, or a friend? Pick a side quest."
                Step.EXTEND -> "Five bonus minutes unlocked — but you have to name them."
            }
            Personality.NERD -> when (step) {
                Step.NUDGE -> "$t ($pct% of plan)."
                Step.REFLECT -> "$came. On-purpose items: ${ctx.matched}. Marginal value is dropping."
                Step.FRICTION -> "Data says pauses cut overruns. 5-second pause, then state your reason."
                Step.REDIRECT -> "Swapping activity now has the highest expected payoff."
                Step.EXTEND -> "+5 min logged as a conscious decision. Reason required."
            }
            Personality.STRICT -> when (step) {
                Step.NUDGE -> "$t. Stay on plan."
                Step.REFLECT -> "$came. Is that still what you're doing?"
                Step.FRICTION -> "Stop. Breathe. Justify the next minute."
                Step.REDIRECT -> "Close the feed. Do something else."
                Step.EXTEND -> "Five minutes. Give a real reason."
            }
        }
    }
}
