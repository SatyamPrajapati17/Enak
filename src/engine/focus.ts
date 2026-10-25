/** Pure focus/permission/streak helpers. */
export type PermState = 'granted' | 'denied' | 'prompt' | 'unsupported';

export function mapPermission(raw: string | undefined | null): PermState {
  if (raw === 'granted') return 'granted';
  if (raw === 'denied') return 'denied';
  if (raw === 'default' || raw === 'prompt') return 'prompt';
  return 'unsupported';
}

export const PERM_LABEL: Record<PermState, string> = {
  granted: 'Allowed',
  denied: 'Blocked',
  prompt: 'Not asked yet',
  unsupported: 'Not supported here',
};

/** 'ok' under 80%, 'near' from 80%, 'over' at or past the limit. No limit → 'ok'. */
export function limitStatus(usedMin: number, limitMin: number | undefined): 'ok' | 'near' | 'over' {
  if (!limitMin || limitMin <= 0) return 'ok';
  if (usedMin >= limitMin) return 'over';
  if (usedMin >= limitMin * 0.8) return 'near';
  return 'ok';
}

export const dayKey = (t: number) => {
  const d = new Date(t);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

/** Consecutive days (ending today, or yesterday if nothing today yet) with at least one session. */
export function streak(starts: number[], now: number): number {
  const days = new Set(starts.map(dayKey));
  const d = new Date(now);
  if (!days.has(dayKey(d.getTime()))) d.setDate(d.getDate() - 1);
  let n = 0;
  while (days.has(dayKey(d.getTime()))) {
    n++;
    d.setDate(d.getDate() - 1);
  }
  return n;
}

/** Whole minutes elapsed since a live tracking start (never negative). */
export function liveMinutes(start: number, now: number): number {
  return Math.max(0, Math.floor((now - start) / 60000));
}

/** True when hour h falls in quiet hours [start, end), wrapping past midnight. */
export function inQuietHours(h: number, start: number, end: number): boolean {
  if (start === end) return false;
  return start < end ? h >= start && h < end : h >= start || h < end;
}

/** Minutes until the daily limit is reached (0 if already over). */
export function minutesToLimit(usedMin: number, limitMin: number): number {
  return Math.max(0, limitMin - usedMin);
}
