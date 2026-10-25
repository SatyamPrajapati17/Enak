import type { Purpose } from './scoring.ts';
export type { Purpose };

export type Mood = 'bored' | 'stressed' | 'tired' | 'curious' | 'calm';

export interface SessionRecord {
  id: string;
  start: number;
  end: number;
  purpose: Purpose;
  plannedMin: number;
  actualMin: number;
  goal?: string | undefined;
  mood: Mood;
  avgScore: number;
  timeline: { t: number; score: number }[];
  interventions: { step: string; arm: string; accepted: boolean; at: number }[];
  extensions: { reason: string; at: number }[];
  exitLoops: number;
  achieved?: boolean | undefined;
  regret?: number | undefined; // 0-10
  journal?: string | undefined;
  sentiment?: 'positive' | 'neutral' | 'negative' | undefined;
}

export const median = (xs: number[]) => {
  if (!xs.length) return 0;
  const s = [...xs].sort((a, b) => a - b);
  const m = Math.floor(s.length / 2);
  return s.length % 2 ? s[m]! : (s[m - 1]! + s[m]!) / 2;
};

export function baselineMinutes(sessions: SessionRecord[]) {
  return median(sessions.slice(-20).map((s) => s.actualMin)) || 10;
}

export const isOverrun = (s: SessionRecord) => s.actualMin > s.plannedMin * 1.1;

export function metrics(sessions: SessionRecord[]) {
  const n = sessions.length || 1;
  const allInt = sessions.flatMap((s) => s.interventions);
  const rated = sessions.filter((s) => s.regret !== undefined);
  return {
    overrunRate: sessions.filter(isOverrun).length / n,
    avgRegret: rated.length ? rated.reduce((a, s) => a + (s.regret ?? 0), 0) / rated.length : 0,
    acceptance: allInt.length ? allInt.filter((i) => i.accepted).length / allInt.length : 0,
    achievedRate: sessions.filter((s) => s.achieved).length / n,
    avgScore: sessions.reduce((a, s) => a + s.avgScore, 0) / n,
  };
}

/** 7x24 grid of average overrun minutes, Monday first. */
export function riskHeatmap(sessions: SessionRecord[]) {
  const grid = Array.from({ length: 7 }, () => Array(24).fill(0) as number[]);
  const cnt = Array.from({ length: 7 }, () => Array(24).fill(0) as number[]);
  for (const s of sessions) {
    const d = new Date(s.start);
    const day = (d.getDay() + 6) % 7;
    grid[day]![d.getHours()]! += Math.max(0, s.actualMin - s.plannedMin);
    cnt[day]![d.getHours()]!++;
  }
  return grid.map((row, i) => row.map((v, h) => (cnt[i]![h] ? v / cnt[i]![h]! : 0)));
}

export function riskyHours(sessions: SessionRecord[], top = 3): number[] {
  const byHour = Array(24).fill(0) as number[];
  for (const s of sessions) byHour[new Date(s.start).getHours()]! += Math.max(0, s.actualMin - s.plannedMin);
  return byHour
    .map((v, h) => ({ v, h }))
    .filter((x) => x.v > 0)
    .sort((a, b) => b.v - a.v)
    .slice(0, top)
    .map((x) => x.h);
}

const part = (h: number) => (h < 12 ? 'morning' : h < 17 ? 'afternoon' : h < 22 ? 'evening' : 'late night');

/** Finds mood + time-of-day combos that lead to much longer overruns than usual. */
export function discoverTriggers(sessions: SessionRecord[]) {
  const groups = new Map<string, SessionRecord[]>();
  for (const s of sessions) {
    const k = `${s.mood}|${part(new Date(s.start).getHours())}`;
    groups.set(k, [...(groups.get(k) ?? []), s]);
  }
  const overall = sessions.reduce((a, s) => a + s.actualMin / s.plannedMin, 0) / (sessions.length || 1);
  return [...groups.entries()]
    .filter(([, g]) => g.length >= 3)
    .map(([k, g]) => {
      const [mood = '', when = ''] = k.split('|');
      const ratio = g.reduce((a, s) => a + s.actualMin / s.plannedMin, 0) / g.length;
      return { mood, when, ratio, lift: ratio / overall, count: g.length };
    })
    .filter((t) => t.lift > 1.2)
    .sort((a, b) => b.lift - a.lift)
    .slice(0, 4);
}

/** Pick sensible defaults for a new session from history at a similar hour. */
export function smartDefaults(sessions: SessionRecord[], hour: number) {
  const near = sessions.filter((s) => Math.abs(new Date(s.start).getHours() - hour) <= 2).slice(-10);
  if (!near.length) return { purpose: 'quick' as Purpose, plannedMin: 10 };
  const counts = new Map<Purpose, number>();
  near.forEach((s) => counts.set(s.purpose, (counts.get(s.purpose) ?? 0) + 1));
  const purpose = [...counts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? 'quick';
  return { purpose, plannedMin: Math.round(median(near.map((s) => s.plannedMin))) || 10 };
}

/** Converts minutes into a tangible equivalent. */
export function realityCheck(min: number) {
  const opts = [
    { per: 15, label: 'book chapters' },
    { per: 12, label: 'km walked' },
    { per: 5, label: 'cups of chai brewed' },
  ];
  const o = opts[Math.floor(min) % opts.length]!;
  const n = Math.max(1, Math.round(min / o.per));
  return `${n} ${o.label}`;
}
