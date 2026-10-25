// Thompson-sampling bandit that learns which intervention style works for a user.
export type Arm = 'banner' | 'question' | 'pause' | 'suggestion';
export const ARMS: Arm[] = ['banner', 'question', 'pause', 'suggestion'];
export const ARM_NAMES: Record<Arm, string> = {
  banner: 'Time banner',
  question: 'Reflection question',
  pause: 'Breathing pause',
  suggestion: 'Alternative suggestion',
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
  let best: Arm = 'banner';
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
