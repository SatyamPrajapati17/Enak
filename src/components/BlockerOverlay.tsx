import React, { useState } from 'react';
import { BreathingExercise } from './BreathingExercise.tsx';
import { ShieldAlert, Home, Sparkles, AlertOctagon, CheckCircle2, RefreshCw } from 'lucide-react';

interface BlockerOverlayProps {
  appName: string;
  packageName: string;
  warningNumber: number;
  isHardBlock: boolean;
  onCloseApp: () => void;
  onDismissWarning: () => void;
  onNegotiateExtension: (reason: string) => Promise<{ accepted: boolean; message: string }>;
}

export const BlockerOverlay: React.FC<BlockerOverlayProps> = ({
  appName,
  packageName,
  warningNumber,
  isHardBlock,
  onCloseApp,
  onDismissWarning,
  onNegotiateExtension,
}) => {
  const [showBreather, setShowBreather] = useState(false);
  const [showNegotiation, setShowNegotiation] = useState(false);
  const [reason, setReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<{ accepted: boolean; message: string } | null>(null);

  const handleNegotiate = async () => {
    if (!reason.trim()) return;
    setIsSubmitting(true);
    const result = await onNegotiateExtension(reason);
    setFeedback(result);
    setIsSubmitting(false);
    if (result.accepted) {
      setTimeout(() => {
        onDismissWarning();
      }, 1600);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#0B0B10] text-slate-100 select-none animate-fade-in">
      {/* Background ambient mesh */}
      <div className="absolute top-1/4 -left-20 w-80 h-80 rounded-full bg-rose-600/15 blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 -right-20 w-80 h-80 rounded-full bg-[#8A2BE2]/20 blur-3xl pointer-events-none" />

      {showBreather ? (
        <div className="w-full max-w-sm bg-[#161624] border border-cyan-500/30 rounded-3xl p-6 shadow-2xl">
          <BreathingExercise
            durationSeconds={20}
            onComplete={() => {
              setShowBreather(false);
              if (!isHardBlock) onDismissWarning();
            }}
            onCancel={() => setShowBreather(false)}
          />
        </div>
      ) : showNegotiation ? (
        <div className="w-full max-w-sm bg-[#161626] border border-white/10 rounded-3xl p-6 shadow-2xl">
          <div className="flex items-center gap-2 text-cyan-300 text-xs font-semibold uppercase tracking-wider mb-2">
            <Sparkles className="w-4 h-4" />
            <span>AI Time Negotiation</span>
          </div>
          <h3 className="text-lg font-bold text-white mb-2">
            Need 5 more minutes on {appName}?
          </h3>
          <p className="text-xs text-slate-400 mb-4 leading-relaxed">
            State your clear purpose. Enak accepts specific, non-habitual reasons (e.g., &quot;revising notes for class&quot;).
          </p>

          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Why do you need to continue?"
            rows={3}
            className="w-full px-3.5 py-2.5 rounded-xl bg-[#0B0B10] border border-slate-700 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-400 resize-none mb-4"
          />

          {feedback && (
            <div
              className={`p-3 rounded-xl border text-xs mb-4 leading-relaxed ${
                feedback.accepted
                  ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-200'
                  : 'bg-rose-950/40 border-rose-500/40 text-rose-200'
              }`}
            >
              <div className="font-semibold mb-1 flex items-center gap-1.5">
                {feedback.accepted ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                ) : (
                  <AlertOctagon className="w-4 h-4 text-rose-400" />
                )}
                <span>{feedback.accepted ? 'Accepted' : 'Challenged'}</span>
              </div>
              {feedback.message}
            </div>
          )}

          <div className="flex gap-2">
            <button
              onClick={() => setShowNegotiation(false)}
              className="flex-1 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-xs text-slate-300 font-medium"
            >
              Cancel
            </button>
            <button
              onClick={handleNegotiate}
              disabled={isSubmitting || !reason.trim()}
              className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white text-xs font-semibold disabled:opacity-50 flex items-center justify-center gap-1.5"
            >
              {isSubmitting ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  <span>Checking...</span>
                </>
              ) : (
                'Submit'
              )}
            </button>
          </div>
        </div>
      ) : (
        <div className="w-full max-w-sm flex flex-col items-center text-center">
          {/* Badge */}
          <div
            className={`px-3.5 py-1 rounded-full text-xs font-semibold tracking-wide border mb-6 inline-flex items-center gap-1.5 ${
              isHardBlock
                ? 'bg-rose-500/15 border-rose-500/50 text-rose-300'
                : 'bg-[#8A2BE2]/20 border-[#8A2BE2]/40 text-cyan-300'
            }`}
          >
            {isHardBlock ? (
              <>
                <AlertOctagon className="w-3.5 h-3.5 text-rose-400" />
                <span>Hard Boundary Reached</span>
              </>
            ) : (
              <>
                <ShieldAlert className="w-3.5 h-3.5 text-cyan-400" />
                <span>Boundary Warning {warningNumber} of 2</span>
              </>
            )}
          </div>

          {/* App Icon Representation */}
          <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-[#1E1B2E] to-[#2B2348] border border-white/15 flex items-center justify-center mb-5 shadow-xl">
            <span className="text-2xl font-bold text-white tracking-wider">
              {appName.slice(0, 2).toUpperCase()}
            </span>
          </div>

          <h2 className="text-2xl font-bold text-white mb-2 tracking-tight">
            {isHardBlock ? `Step away from ${appName}` : `Pause on ${appName}`}
          </h2>

          <p className="text-sm text-slate-300 max-w-xs leading-relaxed mb-8">
            {isHardBlock
              ? `You've reached your daily limit for ${appName}. Give your mind room to decompress.`
              : `You've reached your planned boundary. Check your intentionality before continuing.`}
          </p>

          <div className="w-full space-y-3">
            {/* Primary Action: Close app */}
            <button
              onClick={onCloseApp}
              className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white font-semibold text-sm hover:brightness-110 transition-all shadow-lg shadow-[#8A2BE2]/25 flex items-center justify-center gap-2"
            >
              <Home className="w-4 h-4" />
              <span>Close App & Go Home</span>
            </button>

            {/* Breather */}
            <button
              onClick={() => setShowBreather(true)}
              className="w-full py-3 rounded-2xl bg-white/5 hover:bg-white/10 text-cyan-300 border border-cyan-400/20 font-medium text-xs transition-colors"
            >
              Take a 20s Breather
            </button>

            {!isHardBlock && (
              <button
                onClick={() => setShowNegotiation(true)}
                className="w-full py-2 text-xs text-slate-400 hover:text-slate-200 transition-colors"
              >
                Ask AI Coach for 5 more minutes
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
