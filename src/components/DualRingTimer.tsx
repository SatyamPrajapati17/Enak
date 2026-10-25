import React from 'react';

interface DualRingTimerProps {
  elapsedSec: number;
  plannedSec: number;
  score: number;
  label: 'Focused' | 'Drifting' | 'Compulsive';
  size?: number;
}

export const DualRingTimer: React.FC<DualRingTimerProps> = ({
  elapsedSec,
  plannedSec,
  score,
  label,
  size = 240,
}) => {
  const strokeWidth = 10;
  const outerRadius = (size - strokeWidth * 2) / 2;
  const innerRadius = outerRadius - strokeWidth - 6;
  const center = size / 2;

  const outerCircumference = 2 * Math.PI * outerRadius;
  const innerCircumference = 2 * Math.PI * innerRadius;

  // Outer ring: Elapsed progress relative to planned (can exceed 100%)
  const outerRatio = plannedSec > 0 ? elapsedSec / plannedSec : 0;
  const outerProgress = Math.min(outerRatio, 1);
  const outerDashoffset = outerCircumference * (1 - outerProgress);

  // Inner ring: Score / Intentionality ratio
  const innerRatio = Math.min(Math.max(score / 100, 0), 1);
  const innerDashoffset = innerCircumference * (1 - innerRatio);

  const formatTime = (totalSec: number) => {
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  };

  const getLabelColor = () => {
    if (label === 'Focused') return '#22E3F0'; // Electric Cyan
    if (label === 'Drifting') return '#F59E0B'; // Amber
    return '#F43F5E'; // Soft Pink
  };

  return (
    <div className="relative flex flex-col items-center justify-center">
      <svg width={size} height={size} className="transform -rotate-90">
        <defs>
          <linearGradient id="amethystGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#8A2BE2" />
            <stop offset="100%" stopColor="#A855F7" />
          </linearGradient>
          <linearGradient id="cyanGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#22E3F0" />
            <stop offset="100%" stopColor="#06B6D4" />
          </linearGradient>
          <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feComposite in="SourceGraphic" in2="blur" operator="over" />
          </filter>
        </defs>

        {/* Outer Ring Background */}
        <circle
          cx={center}
          cy={center}
          r={outerRadius}
          stroke="#1F1F2E"
          strokeWidth={strokeWidth}
          fill="transparent"
        />

        {/* Outer Ring Progress (Elapsed) */}
        <circle
          cx={center}
          cy={center}
          r={outerRadius}
          stroke="url(#amethystGradient)"
          strokeWidth={strokeWidth}
          strokeDasharray={outerCircumference}
          strokeDashoffset={outerDashoffset}
          strokeLinecap="round"
          fill="transparent"
          filter="url(#glow)"
          style={{ transition: 'stroke-dashoffset 0.5s ease' }}
        />

        {/* Inner Ring Background */}
        <circle
          cx={center}
          cy={center}
          r={innerRadius}
          stroke="#181824"
          strokeWidth={strokeWidth - 2}
          fill="transparent"
        />

        {/* Inner Ring Progress (Intentionality Score) */}
        <circle
          cx={center}
          cy={center}
          r={innerRadius}
          stroke="url(#cyanGradient)"
          strokeWidth={strokeWidth - 2}
          strokeDasharray={innerCircumference}
          strokeDashoffset={innerDashoffset}
          strokeLinecap="round"
          fill="transparent"
          style={{ transition: 'stroke-dashoffset 0.5s ease' }}
        />
      </svg>

      {/* Center Digital Display */}
      <div className="absolute inset-0 flex flex-col items-center justify-center text-center select-none">
        <span className="text-3xl font-bold font-mono tracking-tight text-white drop-shadow-md">
          {formatTime(elapsedSec)}
        </span>
        <span className="text-xs text-slate-400 mt-0.5">
          of {Math.round(plannedSec / 60)} min planned
        </span>

        <div
          className="mt-2 px-2.5 py-0.5 rounded-full text-[11px] font-semibold tracking-wide border"
          style={{
            borderColor: getLabelColor(),
            color: getLabelColor(),
            backgroundColor: `${getLabelColor()}15`,
          }}
        >
          {label} ({score})
        </div>
      </div>
    </div>
  );
};
