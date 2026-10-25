# Prompt: Build the native Android version of "Enak"

> Paste this whole file into Google AI Studio. It is self-contained: the original TypeScript rules are included at the end, under "Appendix: source code".

---

## Role
You are a senior Android engineer. Build a **production-quality native Android app named "Enak"** in **Kotlin + Jetpack Compose (Material 3)**, using the web app described here and the TypeScript source in the Appendix (TanStack Start / React / TypeScript) as the **source of truth for features, rules, copy, and design**. Do not port the web UI line by line. Re-implement it natively and add the device-level features a web app cannot have: **real app usage tracking, an app blocker, and per-app timers**.

Never use the words "hackathon", "competition", or "event" anywhere in the app, code comments, or docs.

## Product summary
Enak asks the user **what they want to do and for how long** before a session. It detects when actual usage **drifts from that intention** and responds with **graduated, personal interventions instead of only blocking**. Long screen time is not treated as bad by default. Only **loss of intentionality** matters (intentional long-form learning is fine; compulsive switching and repeated exit attempts are not).

## Where to read logic in the codebase (port these as pure Kotlin, with unit tests)
| Web file | Port to | What it does |
|---|---|---|
| `src/lib/engine/scoring.ts` | `engine/Scoring.kt` | Intentionality score (0–100), purposes (learn, social, relax, research, quick), labels like "Focused" / "Drifting" / "Compulsive" |
| `src/lib/engine/ladder.ts` | `engine/Ladder.kt` | Graduated intervention ladder (nudge → question → breather → negotiate → firm stop), coach personalities calm/funny/nerd/strict |
| `src/lib/engine/bandit.ts` | `engine/Bandit.kt` | Thompson-sampling style experiment choosing which check-in works best per user |
| `src/lib/engine/patterns.ts` | `engine/Patterns.kt` | Overrun rate, regret, risky hours, mood/time triggers, metrics |
| `src/lib/engine/warnings.ts` | `engine/Warnings.kt` | **Max 2 warnings per session**, ≥60 s apart, fired at planned end or on drift; the count persists across process death |
| `src/lib/engine/focus.ts` | `engine/Focus.kt` | Limit status (ok / near at 80% / over at 100%), streak, live minutes, quiet hours that wrap past midnight |
| `src/test/*.test.ts` | `test/…Test.kt` | Port every existing test case with the same numbers. All must pass. |

Keep the engine **free of Android imports** (plain Kotlin module `:engine`) so it is unit-testable on the JVM.

## Screens (match the web routes)
1. **Welcome / sign-up** (`/welcome`): "Sign up with Google" (Credential Manager + Firebase Auth or Supabase Auth), "Continue without an account".
2. **Onboarding** (`/onboarding`): name, coach personality, night hours, weekly budget, language (English / Hinglish / Hindi).
3. **Today** (`/`): greeting, sessions on purpose, time on purpose, calm resets, streak, weekly goal, risky-hour banner, quick-start purposes.
4. **Intent** (`/intent`): purpose, duration, goal, mood, optional learning video.
5. **Session** (`/session`): live dual ring (planned vs elapsed), drift detection, intervention overlays, two-warning popup (spoken with TTS plus vibration), "5 more minutes" negotiation with the AI, finish → recap.
6. **Recap** (`/wrap/$id`): regret slider, goal achieved, journal, confetti on completion.
7. **Insights**, **Lab (experiments)**, **Garden** (a plant that grows with intentional sessions), **Buddy** (share a weekly summary code).
8. **Coach** (`/coach`): chat plus voice input (SpeechRecognizer) and spoken replies (TextToSpeech), quick prompts, Stop button, grounded in the user's stats summary (see `summary()` in `src/routes/coach.tsx`).
9. **Focus & permissions** (`/focus`): **real** per-app daily limits, app blocker, live tracking, permissions centre, quiet hours.
10. **Settings**: export/delete data, account, language, voice.

Learning videos (from `src/lib/videos.ts`): AI in 15 min `j5VtrvjA_LM`, English `m-OCMzN4trc`, Fitness `BGXGdUj93BM`, Web developer roadmap `-4e3ewcTupM`. Play them only after media consent (YouTube Android Player or WebView with youtube-nocookie).

## Native features the web app could NOT do (main goal)
### 1. Real app usage tracking
- Use `UsageStatsManager.queryEvents()` (ACTIVITY_RESUMED / ACTIVITY_PAUSED) to compute **per-app foreground time today** and the **current foreground app**.
- Permission: `android.permission.PACKAGE_USAGE_STATS` (special access). Detect it with `AppOpsManager.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS…)` and deep-link to `Settings.ACTION_USAGE_ACCESS_SETTINGS`.
- Poll every 1–2 s only while a foreground service is active. Otherwise use WorkManager every 15 min for daily summaries.

### 2. App blocker / per-app timers (Enak's own "Digital Wellbeing")
- **Important:** Google's Digital Wellbeing timers have **no public API**, so third-party apps cannot set them. Implement Enak's **own** equivalent:
  - Per-app daily limit (10–120 min), focus schedules, quiet hours.
  - When a limited app is opened past its limit, or during focus mode, show a **full-screen blocking screen** (an Activity launched with `FLAG_ACTIVITY_NEW_TASK`, or a `SYSTEM_ALERT_WINDOW` overlay). It offers: "Close app" (go Home), "Take a breather" (the existing breathing exercise), "Ask for 5 more minutes" (AI negotiation, max once per app per day), with **2 warnings before a hard block**, following the Warnings engine.
  - Detection options (implement both, prefer the first):
    a) A **foreground service** (`foregroundServiceType="specialUse"` and/or `dataSync`) that polls UsageStats for the current app.
    b) An optional **AccessibilityService** (`TYPE_WINDOW_STATE_CHANGED`) for instant detection. It must have a clear disclosure screen and follow the Play policy for accessibility use.
- Permissions: `PACKAGE_USAGE_STATS`, `SYSTEM_ALERT_WINDOW` (via `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`), `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` (Android 13+), `RECEIVE_BOOT_COMPLETED` (restart the service), `QUERY_ALL_PACKAGES` **or** a `<queries>` block listing launcher intents (prefer `<queries>` for Play compliance), `VIBRATE`, `WAKE_LOCK`, `RECORD_AUDIO` (voice coach).
- Ask for battery optimisation exemption (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) with an explanation, so the blocker survives Doze.

### 3. Permissions centre (native version of `/focus`)
One row per permission, showing live status (Allowed / Blocked / Not asked), why it is needed, and a button that opens the exact system screen: Usage access, Display over other apps, Notifications, Accessibility (optional), Microphone, Battery optimisation, Exact alarms (if used). Re-check status in `onResume`.

### 4. Notifications
Channels: "Session check-ins", "Limits", "Daily summary". Respect quiet hours (port `inQuietHours`). Session-end reminder at the planned end time (AlarmManager or WorkManager).

## AI coach (keep the key server-side)
- Do not embed any API key in the APK. Call a small backend (for example a Firebase Cloud Function or the existing web app's server functions) that proxies to Gemini.
- Use **Gemini** via Google AI Studio for the coach. Port the system prompts from `src/lib/ai/coach.functions.ts` exactly: coach persona, "never shame long screen time", answers under 120 words, language rules, and the negotiation prompt that must start with `ACCEPT:` or `CHALLENGE:`, and the journal sentiment classifier.
- Handle errors: show friendly messages, use a Stop/cancel button, and never retry immediately. Back off on 429.

## Data and privacy
- Local-first: Room database for sessions, interventions, app usage per day, settings (DataStore). Session history never leaves the device unless the user exports it.
- Account (optional): Google sign-in, with a private profile storing only name, avatar and preferences.
- Export as JSON, and "Delete everything".
- First launch: show a clear consent screen for usage access and accessibility, explaining exactly what is read (app package names and durations only, never screen content).

## Design (match the web app)
- Dark theme: **Jet Black** background (`oklch(0.12 0.006 270)` ≈ `#0B0B10`), **Royal Amethyst** primary (`oklch(0.54 0.24 300)` ≈ `#8A2BE2`-ish), **Electric Cyan** accent (`oklch(0.86 0.15 195)` ≈ `#22E3F0`), soft pink for warnings.
- Font: **Poppins**. Glass cards (translucent surface with a 1 px light border and blur), gradient primary buttons (amethyst → blue), pill bottom navigation: **Today · Session · Insights · Lab**.
- Animated slow-moving gradient background. Respect "reduce motion".
- Must not look AI-generated: no emoji-heavy UI, no generic purple-on-white, real spacing hierarchy.
- App icon: reuse `public/icons/enak-512.png` and `enak-maskable.png` as the adaptive icon.

## Architecture
- Modules: `:app` (Compose UI, navigation), `:engine` (pure Kotlin), `:data` (Room, DataStore, repositories), `:tracking` (UsageStats, foreground service, accessibility service, blocker activity).
- MVVM with StateFlow, Hilt for DI, Navigation Compose, Coroutines.
- minSdk 26, target/compileSdk 35. Gradle Kotlin DSL with a version catalog.

## Deliverables
1. The complete Android Studio project, ready to build with `./gradlew assembleDebug`.
2. `README.md` covering how to build a debug APK, how to build a signed release APK/AAB (`./gradlew assembleRelease` / `bundleRelease` with a keystore), the permissions explained, and the Play Store policy notes (Usage Access, Accessibility and `QUERY_ALL_PACKAGES` declarations).
3. Unit tests for all engine files, with the same cases as `src/test/`, plus an instrumented test for the blocker flow.
4. Privacy policy text (required by Play for usage access and accessibility).

## Acceptance checklist
- [ ] Granting usage access shows real minutes for Instagram, YouTube and others on the Focus screen, updating live.
- [ ] Setting a 10-minute limit on an app and using it past 8 minutes gives a warning (80%). At 10 minutes the blocking screen appears, with 2 warnings maximum before the hard block.
- [ ] Focus mode blocks the selected apps immediately. Quiet hours suppress notifications.
- [ ] The blocker survives app kill and reboot (foreground service plus boot receiver).
- [ ] Coach answers through the backend with Gemini. Voice in and out works.
- [ ] The session flow works end to end: intent → live session → 2 warnings → finish → recap → confetti.
- [ ] All engine tests pass. The app has no crashes on Android 8 to 15.


## Appendix: source code (port these exactly to Kotlin)

### src/lib/engine/bandit.ts
```ts
// Thompson-sampling bandit that learns which intervention style works for a user.
export type Arm = "banner" | "question" | "pause" | "suggestion";
export const ARMS: Arm[] = ["banner", "question", "pause", "suggestion"];
export const ARM_NAMES: Record<Arm, string> = {
  banner: "Time banner",
  question: "Reflection question",
  pause: "Breathing pause",
  suggestion: "Alternative suggestion",
};

export type ArmStats = Record<Arm, { wins: number; trials: number }>;

export const emptyStats = (): ArmStats =>
  Object.fromEntries(ARMS.map((a) => [a, { wins: 0, trials: 0 }])) as ArmStats;

function gaussian(rng: () => number) {
  const u = Math.max(rng(), 1e-9);
  return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * rng());
}
// Marsaglia–Tsang gamma sampler (shape >= 1)
function gamma(k: number, rng: () => number): number {
  if (k < 1) return gamma(k + 1, rng) * Math.pow(rng(), 1 / k);
  const d = k - 1 / 3;
  const c = 1 / Math.sqrt(9 * d);
  for (;;) {
    let x: number, v: number;
    do {
      x = gaussian(rng);
      v = 1 + c * x;
    } while (v <= 0);
    v = v * v * v;
    if (Math.log(rng()) < 0.5 * x * x + d - d * v + d * Math.log(v)) return d * v;
  }
}
export function betaSample(a: number, b: number, rng: () => number = Math.random) {
  const x = gamma(a, rng);
  return x / (x + gamma(b, rng));
}

export function pickArm(stats: ArmStats, rng: () => number = Math.random): Arm {
  let best: Arm = "banner";
  let bestV = -1;
  for (const a of ARMS) {
    const s = stats[a];
    const v = betaSample(s.wins + 1, s.trials - s.wins + 1, rng);
    if (v > bestV) {
      bestV = v;
      best = a;
    }
  }
  return best;
}

export function record(stats: ArmStats, arm: Arm, success: boolean): ArmStats {
  const s = stats[arm];
  return { ...stats, [arm]: { wins: s.wins + (success ? 1 : 0), trials: s.trials + 1 } };
}

export function successRate(s: { wins: number; trials: number }) {
  return s.trials ? s.wins / s.trials : 0;
}

```

### src/lib/engine/focus.ts
```ts
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
  while (days.has(dayKey(d.getTime()))) { n++; d.setDate(d.getDate() - 1); }
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

```

### src/lib/engine/ladder.ts
```ts
import type { Label, OverrunState, Purpose } from "./scoring";

export type Step = "nudge" | "reflect" | "friction" | "redirect" | "extend";
export const LADDER: Step[] = ["nudge", "reflect", "friction", "redirect", "extend"];

export const STEP_NAMES: Record<Step, string> = {
  nudge: "Gentle nudge",
  reflect: "Reflection prompt",
  friction: "Soft friction",
  redirect: "Redirect",
  extend: "Conscious extension",
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
  if (i.label === "Focused" && !i.exitLoop) return null;
  let target = -1;
  if (i.overrun === "nearing") target = 0;
  if (i.overrun === "over") target = i.label === "Compulsive" ? 2 : 1;
  if (i.overrun === "far-over") target = i.label === "Compulsive" ? 3 : 2;
  if (i.exitLoop) target = Math.max(target, 1);
  if (i.overrun === "on-track" && i.label === "Compulsive") target = Math.max(target, 0);
  if (i.sleepGuard && target >= 0) target = Math.min(target + 1, 3);
  if (i.gentleMode) target = Math.min(target, 1);
  if (target < 0 || target <= i.shown) return null;
  return LADDER[target] ?? null;
}

export type Personality = "calm" | "funny" | "nerd" | "strict";

const PURPOSE_WORD: Record<Purpose, string> = {
  learn: "learn",
  relax: "relax",
  social: "catch up with people",
  research: "research something",
  quick: "do a quick check",
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
      reflect: `${came}. ${ctx.goal ? `Did you find "${ctx.goal}"?` : "Found what you were looking for?"}`,
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

```

### src/lib/engine/patterns.ts
```ts
import type { Purpose } from "./scoring";

export type Mood = "bored" | "stressed" | "tired" | "curious" | "calm";

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
  sentiment?: "positive" | "neutral" | "negative" | undefined;
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

const part = (h: number) => (h < 12 ? "morning" : h < 17 ? "afternoon" : h < 22 ? "evening" : "late night");

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
      const [mood = "", when = ""] = k.split("|");
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
  if (!near.length) return { purpose: "quick" as Purpose, plannedMin: 10 };
  const counts = new Map<Purpose, number>();
  near.forEach((s) => counts.set(s.purpose, (counts.get(s.purpose) ?? 0) + 1));
  const purpose = [...counts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? "quick";
  return { purpose, plannedMin: Math.round(median(near.map((s) => s.plannedMin))) || 10 };
}

/** Converts minutes into a tangible equivalent. */
export function realityCheck(min: number) {
  const opts = [
    { per: 15, label: "book chapters" },
    { per: 12, label: "km walked" },
    { per: 5, label: "cups of chai brewed" },
  ];
  const o = opts[Math.floor(min) % opts.length]!;
  const n = Math.max(1, Math.round(min / o.per));
  return `${n} ${o.label}`;
}

```

### src/lib/engine/scoring.ts
```ts
// Intentionality Score: estimates whether a session is still deliberate.
// Long time alone never lowers the score much; it only matters together with
// fast, shallow, off-purpose behaviour.

export type Purpose = "learn" | "relax" | "social" | "research" | "quick";
export type Label = "Focused" | "Drifting" | "Compulsive";
export type OverrunState = "on-track" | "nearing" | "over" | "far-over";

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
  if (score >= 65) return "Focused";
  if (score >= 40) return "Drifting";
  return "Compulsive";
}

export function overrunState(elapsedSec: number, plannedMin: number): OverrunState {
  const r = elapsedSec / (plannedMin * 60);
  if (r >= 1.5) return "far-over";
  if (r >= 1) return "over";
  if (r >= 0.8) return "nearing";
  return "on-track";
}

export const EXIT_LOOP_WINDOW_MS = 120_000;
export const EXIT_LOOP_THRESHOLD = 3;

/** True when the user left and came back >= 3 times within 2 minutes. */
export function detectExitLoop(returnTimes: number[], now: number): boolean {
  return returnTimes.filter((t) => now - t <= EXIT_LOOP_WINDOW_MS).length >= EXIT_LOOP_THRESHOLD;
}

```

### src/lib/engine/warnings.ts
```ts
/** Two warnings per session: at the plan and one minute later, or on drift. */
export function warningDue(elapsedSec: number, plannedMin: number, count: number, lastAt: number, now: number, drifting: boolean) {
  if (count >= 2 || now - lastAt < 60_000) return false;
  return (plannedMin < 600 && elapsedSec >= plannedMin * 60) || drifting;
}
```

### src/lib/videos.ts
```ts
export const LEARNING_VIDEOS = [
  { id: 'j5VtrvjA_LM', title: 'Learn AI in 15 minutes', category: 'Artificial intelligence' },
  { id: 'm-OCMzN4trc', title: 'English learning session', category: 'Language' },
  { id: 'BGXGdUj93BM', title: 'How to start fitness', category: 'Fitness' },
  { id: '-4e3ewcTupM', title: 'Web developer roadmap', category: 'Development' },
] as const;
```

### src/lib/ai/coach.functions.ts
```ts
import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";

const LANG = {
  en: "Reply in clear, simple English.",
  hinglish: "Reply in friendly Hinglish (Hindi words in Latin script mixed with English).",
  hi: "Reply in simple Hindi using Devanagari script.",
} as const;

const PERSONA = {
  calm: "a calm, kind coach",
  funny: "a light-hearted, witty friend",
  nerd: "a precise, data-loving analyst",
  strict: "a firm but respectful mentor",
} as const;

type Result = { ok: true; text: string } | { ok: false; error: string };

async function run(messages: { role: "user" | "assistant"; content: string }[], instructions: string): Promise<Result> {
  const { askModel, AiError } = await import("./gateway.server");
  try {
    return { ok: true, text: await askModel(messages, instructions) };
  } catch (e) {
    return { ok: false, error: e instanceof AiError ? e.message : "The coach couldn't answer just now." };
  }
}

export const askCoach = createServerFn({ method: "POST" })
  .inputValidator(
    z.object({
      messages: z.array(z.object({ role: z.enum(["user", "assistant"]), content: z.string().max(2000) })).max(30),
      summary: z.string().max(4000),
      language: z.enum(["en", "hinglish", "hi"]),
      personality: z.enum(["calm", "funny", "nerd", "strict"]),
    }),
  )
  .handler(async ({ data }) =>
    run(
      data.messages,
      `You are Enak's coach, ${PERSONA[data.personality]}. You help people keep scrolling aligned with what they meant to do. Never shame long screen time; only intentionality matters. Ground every answer in the user's data below and keep replies under 120 words. ${LANG[data.language]}\n\nUSER DATA:\n${data.summary}`,
    ),
  );

export const negotiateTime = createServerFn({ method: "POST" })
  .inputValidator(
    z.object({
      reason: z.string().min(1).max(500),
      purpose: z.string().max(40),
      goal: z.string().max(200).optional(),
      elapsedMin: z.number(),
      plannedMin: z.number(),
    }),
  )
  .handler(async ({ data }) => {
    const r = await run(
      [{ role: "user", content: `My reason for 5 more minutes: "${data.reason}"` }],
      `You judge a request for 5 more minutes of scrolling. Session purpose: ${data.purpose}. Goal: ${data.goal ?? "none"}. Elapsed ${data.elapsedMin} of ${data.plannedMin} planned minutes. Accept reasons that are specific and tied to the purpose (finishing a tutorial, replying to a friend). Gently challenge vague ones ("just one more", "bored"). Start your reply with exactly ACCEPT: or CHALLENGE: then one short warm sentence.`,
    );
    if (!r.ok) return { ok: false as const, error: r.error };
    const accepted = r.text.trim().toUpperCase().startsWith("ACCEPT");
    return { ok: true as const, accepted, message: r.text.replace(/^\s*(ACCEPT|CHALLENGE):\s*/i, "") };
  });

export const tagJournal = createServerFn({ method: "POST" })
  .inputValidator(z.object({ text: z.string().min(1).max(2000) }))
  .handler(async ({ data }) => {
    const r = await run(
      [{ role: "user", content: data.text }],
      "Classify the sentiment of this short journal entry about a phone session. Reply with exactly one word: positive, neutral, or negative.",
    );
    if (!r.ok) return { sentiment: "neutral" as const };
    const w = r.text.toLowerCase();
    return { sentiment: w.includes("positive") ? ("positive" as const) : w.includes("negative") ? ("negative" as const) : ("neutral" as const) };
  });

```

### src/test/engine.test.ts
```ts
import { describe, expect, it } from "vitest";
import { computeScore, detectExitLoop, overrunState } from "@/lib/engine/scoring";
import { nextStep } from "@/lib/engine/ladder";
import { emptyStats, pickArm, record } from "@/lib/engine/bandit";

const focused = { scrollVelocity: 150, flickRate: 0, avgDwell: 12, refreshRate: 0, purposeMatch: 1, hour: 15, baselineRatio: 1 };
const compulsive = { scrollVelocity: 2500, flickRate: 25, avgDwell: 0.6, refreshRate: 4, purposeMatch: 0.1, hour: 1, baselineRatio: 4 };

describe("overrun states", () => {
  it("is on track below 80%", () => expect(overrunState(7.9 * 60, 10)).toBe("on-track"));
  it("is nearing at 80%", () => expect(overrunState(8 * 60, 10)).toBe("nearing"));
  it("is over at 100%", () => expect(overrunState(10 * 60, 10)).toBe("over"));
  it("is far over at 150%", () => expect(overrunState(15 * 60, 10)).toBe("far-over"));
});

describe("exit loop", () => {
  it("triggers on 3 returns within 2 minutes", () => expect(detectExitLoop([0, 30_000, 60_000], 100_000)).toBe(true));
  it("ignores returns older than 2 minutes", () => expect(detectExitLoop([0, 30_000, 60_000], 150_000)).toBe(false));
  it("needs at least 3 returns", () => expect(detectExitLoop([90_000, 95_000], 100_000)).toBe(false));
});

describe("intentionality score", () => {
  it("labels deep, on-purpose engagement as Focused", () => expect(computeScore(focused).label).toBe("Focused"));
  it("labels fast off-purpose flicking as Compulsive", () => expect(computeScore(compulsive).label).toBe("Compulsive"));
  it("keeps a very long focused session Focused", () =>
    expect(computeScore({ ...focused, baselineRatio: 6, hour: 23 }).label).toBe("Focused"));
});

describe("intervention ladder", () => {
  const base = { exitLoop: false, shown: -1, sleepGuard: false, gentleMode: false };
  it("never interrupts a focused session, even far over time", () =>
    expect(nextStep({ ...base, overrun: "far-over", label: "Focused" })).toBeNull());
  it("starts with a gentle nudge when nearing the limit", () =>
    expect(nextStep({ ...base, overrun: "nearing", label: "Drifting" })).toBe("nudge"));
  it("uses soft friction when over time and compulsive", () =>
    expect(nextStep({ ...base, overrun: "over", label: "Compulsive" })).toBe("friction"));
  it("does not repeat a step already shown", () =>
    expect(nextStep({ ...base, overrun: "nearing", label: "Drifting", shown: 0 })).toBeNull());
  it("caps at reflection on gentle days", () =>
    expect(nextStep({ ...base, overrun: "far-over", label: "Compulsive", gentleMode: true })).toBe("reflect"));
});

describe("bandit", () => {
  it("prefers an arm that keeps working", () => {
    let s = emptyStats();
    for (let i = 0; i < 40; i++) {
      s = record(s, "question", true);
      s = record(s, "banner", false);
      s = record(s, "pause", false);
      s = record(s, "suggestion", false);
    }
    const picks = Array.from({ length: 50 }, () => pickArm(s));
    expect(picks.filter((p) => p === "question").length).toBeGreaterThan(45);
  });
});

```

### src/test/focus.test.ts
```ts
import { describe, it, expect } from 'vitest';
import { mapPermission, limitStatus, streak } from '@/lib/engine/focus';

describe('focus helpers', () => {
  it('maps browser permission values', () => {
    expect(mapPermission('default')).toBe('prompt');
    expect(mapPermission('granted')).toBe('granted');
    expect(mapPermission('denied')).toBe('denied');
    expect(mapPermission(undefined)).toBe('unsupported');
  });
  it('warns near 80% and at the daily limit', () => {
    expect(limitStatus(23, 30)).toBe('ok');
    expect(limitStatus(24, 30)).toBe('near');
    expect(limitStatus(30, 30)).toBe('over');
    expect(limitStatus(500, undefined)).toBe('ok');
  });
  it('counts consecutive session days', () => {
    const now = new Date(2026, 9, 25, 12).getTime();
    const day = (n: number) => new Date(2026, 9, 25 - n, 10).getTime();
    expect(streak([day(0), day(1), day(2), day(4)], now)).toBe(3);
    expect(streak([day(1), day(2)], now)).toBe(2);
    expect(streak([day(3)], now)).toBe(0);
  });
});

import { liveMinutes, inQuietHours, minutesToLimit } from '@/lib/engine/focus';
describe('live tracking', () => {
  it('counts whole live minutes', () => {
    expect(liveMinutes(0, 59_999)).toBe(0);
    expect(liveMinutes(0, 125_000)).toBe(2);
    expect(liveMinutes(10_000, 0)).toBe(0);
  });
  it('quiet hours wrap past midnight', () => {
    expect(inQuietHours(23, 22, 7)).toBe(true);
    expect(inQuietHours(3, 22, 7)).toBe(true);
    expect(inQuietHours(12, 22, 7)).toBe(false);
  });
  it('minutes left to limit', () => {
    expect(minutesToLimit(20, 30)).toBe(10);
    expect(minutesToLimit(40, 30)).toBe(0);
  });
});

```

### src/test/warnings.test.ts
```ts
import { describe, expect, it } from 'vitest';
import { warningDue } from '@/lib/engine/warnings';
import { LEARNING_VIDEOS } from '@/lib/videos';
describe('session warning rules', () => {
  it('warns twice and never a third time', () => {
    expect(warningDue(900, 15, 0, 0, 100000, false)).toBe(true);
    expect(warningDue(960, 15, 1, 100000, 160000, false)).toBe(true);
    expect(warningDue(1200, 15, 2, 160000, 220000, true)).toBe(false);
  });
  it('waits a minute between warnings', () => {
    expect(warningDue(959, 15, 1, 100000, 159999, false)).toBe(false);
  });
  it('warns on drift but not before an on-purpose plan ends', () => {
    expect(warningDue(100, 15, 0, 0, 100000, false)).toBe(false);
    expect(warningDue(100, 15, 0, 0, 100000, true)).toBe(true);
  });
  it('keeps open-ended sessions open unless drifting', () => {
    expect(warningDue(37000, 600, 0, 0, 100000, false)).toBe(false);
  });
});
it('includes exactly the four supplied YouTube videos', () => {
  expect(LEARNING_VIDEOS.map(v => v.id)).toEqual(['j5VtrvjA_LM', 'm-OCMzN4trc', 'BGXGdUj93BM', '-4e3ewcTupM']);
});
```
