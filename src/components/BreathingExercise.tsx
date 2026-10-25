import React, { useState, useEffect } from 'react';
import { Wind } from 'lucide-react';

interface BreathingExerciseProps {
  onComplete: () => void;
  onCancel?: () => void;
  durationSeconds?: number;
}

export const BreathingExercise: React.FC<BreathingExerciseProps> = ({
  onComplete,
  onCancel,
  durationSeconds = 24,
}) => {
  const [secondsRemaining, setSecondsRemaining] = useState(durationSeconds);
  const [phase, setPhase] = useState<'Inhale' | 'Hold' | 'Exhale'>('Inhale');

  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          onComplete();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [onComplete]);

  // 4s Inhale, 4s Hold, 4s Exhale loop
  useEffect(() => {
    const elapsed = durationSeconds - secondsRemaining;
    const cycle = elapsed % 12;
    if (cycle < 4) {
      setPhase('Inhale');
    } else if (cycle < 8) {
      setPhase('Hold');
    } else {
      setPhase('Exhale');
    }
  }, [secondsRemaining, durationSeconds]);

  const scale =
    phase === 'Inhale'
      ? 'scale-125'
      : phase === 'Hold'
      ? 'scale-125 opacity-90'
      : 'scale-90 opacity-60';

  return (
    <div className="flex flex-col items-center justify-center p-6 text-center select-none">
      <div className="flex items-center gap-2 mb-6 text-cyan-300">
        <Wind className="w-5 h-5 animate-pulse" />
        <span className="text-xs uppercase tracking-widest font-semibold">Mindful Reset Pause</span>
      </div>

      <div className="relative w-44 h-44 flex items-center justify-center my-4">
        {/* Glowing pulsing aura */}
        <div
          className={`absolute inset-0 rounded-full bg-gradient-to-tr from-[#8A2BE2]/40 to-[#22E3F0]/40 blur-xl transition-all duration-1000 ease-in-out ${scale}`}
        />

        {/* Breathing Circle */}
        <div
          className={`w-36 h-36 rounded-full border-2 border-cyan-400/80 bg-[#161426]/90 flex flex-col items-center justify-center shadow-2xl transition-all duration-1000 ease-in-out ${scale}`}
        >
          <span className="text-xl font-bold text-white tracking-wide">{phase}</span>
          <span className="text-xs text-cyan-300/80 mt-1">Deep belly breath</span>
        </div>
      </div>

      <p className="text-sm text-slate-300 mt-4 max-w-xs">
        Notice your shoulders dropping and tension leaving your eyes.
      </p>

      <div className="mt-6 flex items-center gap-3">
        <span className="text-xs text-slate-400 font-mono">{secondsRemaining}s left</span>
        {onCancel && (
          <button
            onClick={onCancel}
            className="text-xs text-slate-400 hover:text-white underline underline-offset-4 ml-4"
          >
            Skip pause
          </button>
        )}
      </div>
    </div>
  );
};
