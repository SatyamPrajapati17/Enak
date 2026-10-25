// Intentionality Score: estimates whether a session is still deliberate.
// Long time alone never lowers the score much; it only matters together with
// fast, shallow, off-purpose behaviour.

export type Purpose = 'learn' | 'relax' | 'social' | 'research' | 'quick';
export type Label = 'Focused' | 'Drifting' | 'Compulsive';
export type OverrunState = 'on-track' | 'nearing' | 'over' | 'far-over';

export interface Signals {
  /** average scroll speed, px per second */
  scrollVelocity: number;
  /** fast flicks (items skipped in < 1.2s) per minute */
  flickRate: number;
  /** average seconds spent on an item */
  avgDwell: number;
  /** refreshes + revisits per minute */
  refreshRate: number;
  /** 0..1 share of viewed items matching declared purpose */
  purposeMatch: number;
  /** local hour 0-23 */
  hour: number;
  /** elapsed minutes / personal median session minutes */
  baselineRatio: number;
}

const clamp = (v: number, lo = 0, hi = 1) => Math.max(lo, Math.min(hi, v));

export function computeScore(s: Signals): { score: number; label: Label; parts: Record<string, number> } {
  // each part: 1 = intentional, 0 = compulsive
  const velocity = 1 - clamp((s.scrollVelocity - 300) / 1500);
  const flick = 1 - clamp(s.flickRate / 20);
  const dwell = clamp((s.avgDwell - 1) / 9);
  const refresh = 1 - clamp(s.refreshRate / 3);
  const match = clamp(s.purposeMatch);
  const late = s.hour >= 23 || s.hour < 5 ? 0.6 : 1;
  const baseline = 1 - clamp((s.baselineRatio - 1.5) / 2.5);

  const parts = { velocity, flick, dwell, refresh, match, late, baseline };
  // Deep engagement (high dwell + on-purpose) protects long sessions.
  const engagement = dwell * 0.5 + match * 0.5;
  const contextWeight = engagement > 0.7 ? 0.02 : 0.1;
  const raw =
    velocity * 0.15 +
    flick * 0.17 +
    dwell * 0.22 +
    refresh * 0.1 +
    match * 0.24 +
    late * (contextWeight * 0.6) +
    baseline * contextWeight;
  const max = 0.88 + contextWeight * 1.6;
  const score = Math.round(clamp(raw / max) * 100);
  return { score, label: labelFor(score), parts };
}

export function labelFor(score: number): Label {
  if (score >= 65) return 'Focused';
  if (score >= 40) return 'Drifting';
  return 'Compulsive';
}

export function overrunState(elapsedSec: number, plannedMin: number): OverrunState {
  const r = elapsedSec / (plannedMin * 60);
  if (r >= 1.5) return 'far-over';
  if (r >= 1) return 'over';
  if (r >= 0.8) return 'nearing';
  return 'on-track';
}

export const EXIT_LOOP_WINDOW_MS = 120_000;
export const EXIT_LOOP_THRESHOLD = 3;

/** True when the user left and came back >= 3 times within 2 minutes. */
export function detectExitLoop(returnTimes: number[], now: number): boolean {
  return returnTimes.filter((t) => now - t <= EXIT_LOOP_WINDOW_MS).length >= EXIT_LOOP_THRESHOLD;
}
