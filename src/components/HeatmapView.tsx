import React, { useState } from 'react';
import type { SessionRecord } from '../engine/patterns.ts';
import { riskHeatmap, riskyHours, discoverTriggers, realityCheck } from '../engine/patterns.ts';
import { Clock, AlertTriangle, Sparkles, Flame } from 'lucide-react';

interface HeatmapViewProps {
  sessions: SessionRecord[];
}

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

export const HeatmapView: React.FC<HeatmapViewProps> = ({ sessions }) => {
  const [hoveredCell, setHoveredCell] = useState<{ day: string; hour: number; val: number } | null>(null);

  const grid = riskHeatmap(sessions);
  const topRisky = riskyHours(sessions, 3);
  const triggers = discoverTriggers(sessions);

  // Find max value in grid to normalize color gradient
  const maxVal = Math.max(1, ...grid.flatMap((r) => r));

  const getColor = (val: number) => {
    if (val === 0) return 'bg-[#181824] hover:bg-[#252538]';
    const ratio = val / maxVal;
    if (ratio < 0.25) return 'bg-[#8A2BE2]/25 border border-[#8A2BE2]/30';
    if (ratio < 0.6) return 'bg-[#8A2BE2]/60 border border-[#8A2BE2]/50 text-white';
    if (ratio < 0.85) return 'bg-[#A855F7] shadow-sm shadow-purple-500/20 text-white';
    return 'bg-[#F43F5E] shadow-sm shadow-rose-500/30 text-white'; // High overrun
  };

  return (
    <div className="space-y-6">
      {/* 7x24 Overrun Heatmap Card */}
      <div className="p-5 rounded-3xl bg-[#14141E] border border-white/5 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-[#22E3F0]" />
            <h3 className="text-sm font-semibold text-white">7×24 Overrun Risk Heatmap</h3>
          </div>
          <span className="text-[11px] text-slate-400">Monday first · 0h–23h</span>
        </div>

        {/* Heatmap Grid */}
        <div className="overflow-x-auto pb-2">
          <div className="min-w-[420px]">
            {/* Hour labels */}
            <div className="grid grid-cols-[36px_repeat(24,1fr)] gap-1 mb-1.5 text-[9px] text-slate-500 font-mono">
              <span />
              {Array.from({ length: 24 }).map((_, h) => (
                <span key={h} className="text-center">
                  {h % 4 === 0 ? `${h}h` : ''}
                </span>
              ))}
            </div>

            {/* Days & Cells */}
            {grid.map((row, dayIdx) => (
              <div key={dayIdx} className="grid grid-cols-[36px_repeat(24,1fr)] gap-1 mb-1 items-center">
                <span className="text-[10px] font-medium text-slate-400">{DAYS[dayIdx]}</span>
                {row.map((val, hour) => (
                  <div
                    key={hour}
                    onMouseEnter={() => setHoveredCell({ day: DAYS[dayIdx], hour, val })}
                    onMouseLeave={() => setHoveredCell(null)}
                    className={`h-4.5 rounded-sm transition-all cursor-pointer ${getColor(val)}`}
                    title={`${DAYS[dayIdx]} ${hour}:00 — ${val.toFixed(1)}m avg overrun`}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>

        {/* Tooltip & Legend */}
        <div className="flex items-center justify-between pt-3 border-t border-white/5 text-[11px] text-slate-400">
          <div>
            {hoveredCell ? (
              <span className="text-cyan-300 font-mono">
                {hoveredCell.day} at {hoveredCell.hour}:00 →{' '}
                <strong className="text-white">{hoveredCell.val.toFixed(1)}m</strong> avg overrun
              </span>
            ) : (
              <span>Hover any cell to inspect hour</span>
            )}
          </div>
          <div className="flex items-center gap-1.5">
            <span className="text-[10px]">Low</span>
            <div className="w-2.5 h-2.5 rounded-sm bg-[#181824]" />
            <div className="w-2.5 h-2.5 rounded-sm bg-[#8A2BE2]/40" />
            <div className="w-2.5 h-2.5 rounded-sm bg-[#A855F7]" />
            <div className="w-2.5 h-2.5 rounded-sm bg-[#F43F5E]" />
            <span className="text-[10px]">High</span>
          </div>
        </div>
      </div>

      {/* Risky Hours & Triggers */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Risky Hours */}
        <div className="p-4 rounded-2xl bg-[#14141E] border border-white/5">
          <div className="flex items-center gap-2 mb-3">
            <Flame className="w-4 h-4 text-rose-400" />
            <h4 className="text-xs font-semibold text-white uppercase tracking-wider">
              Top Risky Hours
            </h4>
          </div>

          {topRisky.length > 0 ? (
            <div className="space-y-2">
              {topRisky.map((h, idx) => (
                <div
                  key={h}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-[#1B1B28] border border-white/5"
                >
                  <div className="flex items-center gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-rose-500/20 text-rose-400 font-mono text-xs flex items-center justify-center font-bold">
                      #{idx + 1}
                    </span>
                    <span className="text-sm font-medium text-slate-200">
                      {h}:00 – {h + 1}:00
                    </span>
                  </div>
                  <span className="text-[11px] text-rose-300 font-mono">
                    {h >= 22 || h < 5 ? 'Night drift' : 'High overrun'}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400">
              No recurring risky hours detected yet. Keep logging your sessions!
            </p>
          )}
        </div>

        {/* Behavioral Triggers */}
        <div className="p-4 rounded-2xl bg-[#14141E] border border-white/5">
          <div className="flex items-center gap-2 mb-3">
            <AlertTriangle className="w-4 h-4 text-amber-400" />
            <h4 className="text-xs font-semibold text-white uppercase tracking-wider">
              Discovered Triggers
            </h4>
          </div>

          {triggers.length > 0 ? (
            <div className="space-y-2">
              {triggers.map((t, idx) => (
                <div
                  key={idx}
                  className="p-2.5 rounded-xl bg-[#1B1B28] border border-white/5 flex items-center justify-between"
                >
                  <div>
                    <span className="text-xs font-semibold text-white capitalize">{t.mood}</span>{' '}
                    <span className="text-xs text-slate-400">in the {t.when}</span>
                  </div>
                  <div className="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 text-[11px] font-mono font-bold">
                    {t.lift.toFixed(1)}x overrun
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 leading-relaxed">
              Triggers automatically unlock after 3+ sessions in similar moods and times.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
