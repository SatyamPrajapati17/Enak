import type { Purpose } from './engine/scoring.ts';
import type { Mood, SessionRecord } from './engine/patterns.ts';
import type { Personality, Step } from './engine/ladder.ts';

export interface UserSettings {
  name: string;
  personality: Personality;
  language: 'en' | 'hinglish' | 'hi';
  weeklyGoalHours: number;
  quietHoursStart: number;
  quietHoursEnd: number;
  gentleMode: boolean;
  sleepGuard: boolean;
  isOnboarded: boolean;
}

export interface AppLimit {
  packageName: string;
  appName: string;
  icon: string;
  category: string;
  usedMinutesToday: number;
  dailyLimitMinutes: number;
  warningCount: number;
  isBlocked: boolean;
  lastWarningTime: number;
}

export interface CoachMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}
