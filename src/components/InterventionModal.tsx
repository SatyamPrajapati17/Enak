import React, { useState } from 'react';
import type { Step, Personality } from '../engine/ladder.ts';
import { promptCopy } from '../engine/ladder.ts';
import type { Purpose } from '../engine/scoring.ts';
import { BreathingExercise } from './BreathingExercise.tsx';
import { AlertCircle, ShieldAlert, Sparkles, X, Compass, CheckCircle2 } from 'lucide-react';

interface InterventionModalProps {
  step?: Step;
  isWarning?: boolean;
  warningCount?: number;
  personality: Personality;
  purpose: Purpose;
  elapsedMin: number;
  plannedMin: number;
  matchedCount: number;
  goal?: string;
  onAccept: () => void;
  onDismiss: () => void;
  onNegotiateExtension: (reason: string) => Promise<{ accepted: boolean; message: string }>;
  onFinishSessionEarly: () => void;
}

export const InterventionModal: React.FC<InterventionModalProps> = ({
  step = 'nudge',
  isWarning = false,
  warningCount = 1,
  personality,
  purpose,
  elapsedMin,
  plannedMin,
  matchedCount,
  goal,
  onAccept,
  onDismiss,
  onNegotiateExtension,
  onFinishSessionEarly,
}) => {
  const [inBreather, setInBreather] = useState(step === 'friction');
  const [extensionReason, setExtensionReason] = useState('');
  const [isNegotiating, setIsNegotiating] = useState(false);
  const [negotiationResult, setNegotiationResult] = useState<{ accepted: boolean; message: string } | null>(null);

  const copy = promptCopy(step, personality, {
    purpose,
    elapsedMin,
    plannedMin,
    matched: matchedCount,
    goal,
  });

  const handleNegotiate = async () => {
    if (!extensionReason.trim()) return;
    setIsNegotiating(true);
    const result = await onNegotiateExtension(extensionReason);
    setNegotiationResult(result);
    setIsNegotiating(false);
    if (result.accepted) {
      setTimeout(() => {
        onAccept();
      }, 1600);
    }
  };

  if (inBreather) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
        <div className="w-full max-w-sm bg-[#161622] border border-cyan-500/30 rounded-3xl p-6 shadow-2xl">
          <BreathingExercise
            durationSeconds={15}
            onComplete={() => {
              setInBreather(false);
              onAccept();
            }}
            onCancel={() => {
              setInBreather(false);
              onAccept();
            }}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-sm bg-[#161624] border border-white/10 rounded-3xl p-6 shadow-2xl relative overflow-hidden">
        {/* Subtle top ambient glow */}
        <div className="absolute -top-12 left-1/2 -translate-x-1/2 w-48 h-24 bg-[#8A2BE2]/30 blur-2xl pointer-events-none" />

        {/* Warning or Intervention Badge */}
        <div className="flex items-center justify-between mb-4">
          <div
            className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border ${
              isWarning
                ? 'bg-rose-500/10 border-rose-500/40 text-rose-300'
                : 'bg-[#8A2BE2]/20 border-[#8A2BE2]/40 text-cyan-300'
            }`}
          >
            {isWarning ? (
              <>
                <ShieldAlert className="w-3.5 h-3.5 text-rose-400" />
                <span>Session Warning {warningCount} of 2</span>
              </>
            ) : (
              <>
                <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                <span className="capitalize">{step} Intervention</span>
              </>
            )}
          </div>

          <button
            onClick={onDismiss}
            className="text-slate-400 hover:text-white transition-colors p-1"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Main Copy */}
        <h3 className="text-lg font-bold text-white mb-2 leading-snug">
          {isWarning ? 'Planned duration reached' : 'Checking in on your drift'}
        </h3>
        <p className="text-sm text-slate-300 leading-relaxed mb-6 font-normal">
          {copy}
        </p>

        {/* Step-Specific Actions */}
        {step === 'extend' && !negotiationResult && (
          <div className="mb-6 space-y-3">
            <label className="text-xs font-medium text-slate-400">
              State your specific purpose for 5 extra minutes:
            </label>
            <input
              type="text"
              value={extensionReason}
              onChange={(e) => setExtensionReason(e.target.value)}
              placeholder="e.g., Finishing the final coding example"
              className="w-full px-3.5 py-2.5 rounded-xl bg-[#0F0F17] border border-slate-700/60 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-400"
            />
            <button
              onClick={handleNegotiate}
              disabled={isNegotiating || !extensionReason.trim()}
              className="w-full py-2.5 rounded-xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white text-xs font-semibold hover:opacity-95 disabled:opacity-50 transition-opacity flex items-center justify-center gap-2"
            >
              {isNegotiating ? 'Consulting Coach...' : 'Ask AI for +5 Minutes'}
            </button>
          </div>
        )}

        {negotiationResult && (
          <div
            className={`mb-6 p-3.5 rounded-2xl border text-xs leading-relaxed ${
              negotiationResult.accepted
                ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-200'
                : 'bg-rose-950/40 border-rose-500/40 text-rose-200'
            }`}
          >
            <div className="font-semibold mb-1 flex items-center gap-1.5">
              {negotiationResult.accepted ? (
                <>
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <span>Extension Granted</span>
                </>
              ) : (
                <>
                  <AlertCircle className="w-4 h-4 text-rose-400" />
                  <span>Coach Challenge</span>
                </>
              )}
            </div>
            {negotiationResult.message}
          </div>
        )}

        {/* Primary Buttons */}
        <div className="flex flex-col gap-2.5">
          {step === 'friction' ? (
            <button
              onClick={() => setInBreather(true)}
              className="w-full py-3 rounded-2xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white font-medium text-sm hover:brightness-110 transition-all shadow-lg shadow-[#8A2BE2]/20"
            >
              Take 15-second Breather
            </button>
          ) : step === 'redirect' ? (
            <button
              onClick={onFinishSessionEarly}
              className="w-full py-3 rounded-2xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white font-medium text-sm hover:brightness-110 transition-all flex items-center justify-center gap-2"
            >
              <Compass className="w-4 h-4" />
              <span>Step Away & Start Side Quest</span>
            </button>
          ) : (
            <button
              onClick={onAccept}
              className="w-full py-3 rounded-2xl bg-[#8A2BE2] text-white font-medium text-sm hover:bg-[#9739ee] transition-colors"
            >
              I'm Conscious & Continuing
            </button>
          )}

          <button
            onClick={onFinishSessionEarly}
            className="w-full py-2.5 rounded-2xl bg-white/5 hover:bg-white/10 text-slate-300 font-normal text-xs transition-colors"
          >
            Wrap Up Session Now
          </button>
        </div>
      </div>
    </div>
  );
};
