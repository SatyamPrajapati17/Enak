import type { Label, OverrunState, Purpose } from './scoring.ts';

export type Step = 'nudge' | 'reflect' | 'friction' | 'redirect' | 'extend';
export const LADDER: Step[] = ['nudge', 'reflect', 'friction', 'redirect', 'extend'];

export const STEP_NAMES: Record<Step, string> = {
  nudge: 'Gentle nudge',
  reflect: 'Reflection prompt',
  friction: 'Soft friction',
  redirect: 'Redirect',
  extend: 'Conscious extension',
};

export interface LadderInput {
  overrun: OverrunState;
  label: Label;
  exitLoop: boolean;
  /** highest step already shown this session, -1 if none */
  shown: number;
  sleepGuard: boolean;
  gentleMode: boolean;
}

/**
 * Decide the next intervention step, or null when nothing should appear.
 * Focused sessions are never interrupted, even when far over time.
 */
export function nextStep(i: LadderInput): Step | null {
  if (i.label === 'Focused' && !i.exitLoop) return null;
  let target = -1;
  if (i.overrun === 'nearing') target = 0;
  if (i.overrun === 'over') target = i.label === 'Compulsive' ? 2 : 1;
  if (i.overrun === 'far-over') target = i.label === 'Compulsive' ? 3 : 2;
  if (i.exitLoop) target = Math.max(target, 1);
  if (i.overrun === 'on-track' && i.label === 'Compulsive') target = Math.max(target, 0);
  if (i.sleepGuard && target >= 0) target = Math.min(target + 1, 3);
  if (i.gentleMode) target = Math.min(target, 1);
  if (target < 0 || target <= i.shown) return null;
  return LADDER[target] ?? null;
}

export type Personality = 'calm' | 'funny' | 'nerd' | 'strict';

const PURPOSE_WORD: Record<Purpose, string> = {
  learn: 'learn',
  relax: 'relax',
  social: 'catch up with people',
  research: 'research something',
  quick: 'do a quick check',
};

export function promptCopy(
  step: Step,
  p: Personality,
  ctx: { purpose: Purpose; elapsedMin: number; plannedMin: number; matched: number; goal?: string | undefined },
): string {
  const came = `You came to ${PURPOSE_WORD[ctx.purpose]}`;
  const t = `${ctx.elapsedMin} of ${ctx.plannedMin} min`;
  const lines: Record<Personality, Record<Step, string>> = {
    calm: {
      nudge: `${t}. No rush — just a heads-up.`,
      reflect: `${came}. ${ctx.goal ? `Did you find "${ctx.goal}"?` : 'Found what you were looking for?'}`,
      friction: `Let's breathe for a moment. Why would you like to keep going?`,
      redirect: `Maybe a glass of water or a short walk would feel good now.`,
      extend: `You can take 5 more minutes — just say why.`,
    },
    funny: {
      nudge: `${t}. Your thumb has run a small marathon.`,
      reflect: `${came}. ${ctx.matched} useful things so far. Victory lap or exit?`,
      friction: `Plot twist: the feed never ends. You do. Why continue?`,
      redirect: `Fridge, window, or a friend? Pick a side quest.`,
      extend: `Five bonus minutes unlocked — but you have to name them.`,
    },
    nerd: {
      nudge: `${t} (${Math.round((ctx.elapsedMin / ctx.plannedMin) * 100)}% of plan).`,
      reflect: `${came}. On-purpose items: ${ctx.matched}. Marginal value is dropping.`,
      friction: `Data says pauses cut overruns. 5-second pause, then state your reason.`,
      redirect: `Swapping activity now has the highest expected payoff.`,
      extend: `+5 min logged as a conscious decision. Reason required.`,
    },
    strict: {
      nudge: `${t}. Stay on plan.`,
      reflect: `${came}. Is that still what you're doing?`,
      friction: `Stop. Breathe. Justify the next minute.`,
      redirect: `Close the feed. Do something else.`,
      extend: `Five minutes. Give a real reason.`,
    },
  };
  return lines[p][step];
}
