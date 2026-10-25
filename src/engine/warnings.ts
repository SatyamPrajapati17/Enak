/** Two warnings per session: at the plan and one minute later, or on drift. */
export function warningDue(
  elapsedSec: number,
  plannedMin: number,
  count: number,
  lastAt: number,
  now: number,
  drifting: boolean,
): boolean {
  if (count >= 2 || now - lastAt < 60_000) return false;
  return (plannedMin < 600 && elapsedSec >= plannedMin * 60) || drifting;
}
