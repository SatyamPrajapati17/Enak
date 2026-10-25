import React, { useState, useEffect, useRef } from 'react';
import confetti from 'canvas-confetti';
import {
  Sparkles,
  Flame,
  Clock,
  Play,
  RotateCcw,
  CheckCircle2,
  ChevronRight,
  Shield,
  Smartphone,
  Code2,
  Sliders,
  Send,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  Award,
  BookOpen,
  Coffee,
  Users,
  Search,
  Zap,
  Leaf,
  Share2,
  AlertTriangle,
  Settings as SettingsIcon,
  Trash2,
  Download,
  Square,
  Bot,
  ExternalLink,
} from 'lucide-react';

import type { UserSettings, AppLimit, CoachMessage } from './types.ts';
import type { Mood, SessionRecord } from './engine/patterns.ts';
import {
  metrics,
  realityCheck,
  smartDefaults,
} from './engine/patterns.ts';
import type { Purpose, Signals } from './engine/scoring.ts';
import { computeScore, overrunState, detectExitLoop } from './engine/scoring.ts';
import type { Personality, Step } from './engine/ladder.ts';
import { nextStep, promptCopy } from './engine/ladder.ts';
import { emptyStats, pickArm, record, successRate, ARM_NAMES } from './engine/bandit.ts';
import type { Arm, ArmStats } from './engine/bandit.ts';
import { limitStatus, inQuietHours, streak } from './engine/focus.ts';
import { warningDue } from './engine/warnings.ts';
import { LEARNING_VIDEOS } from './engine/videos.ts';

import { DualRingTimer } from './components/DualRingTimer.tsx';
import { InterventionModal } from './components/InterventionModal.tsx';
import { BlockerOverlay } from './components/BlockerOverlay.tsx';
import { HeatmapView } from './components/HeatmapView.tsx';
import { AndroidCodeInspector } from './components/AndroidCodeInspector.tsx';
import { EngineTestRunner } from './components/EngineTestRunner.tsx';

// Initial Mock Sessions for rich insights
const INITIAL_SESSIONS: SessionRecord[] = [
  {
    id: 's-1',
    start: Date.now() - 3600000 * 24 * 2 - 3600000 * 3,
    end: Date.now() - 3600000 * 24 * 2 - 3600000 * 2.5,
    purpose: 'learn',
    plannedMin: 20,
    actualMin: 22,
    goal: 'Study Kotlin Coroutines',
    mood: 'curious',
    avgScore: 82,
    timeline: [],
    interventions: [],
    extensions: [],
    exitLoops: 0,
    achieved: true,
    regret: 1,
    journal: 'Felt really productive and learned flow builders.',
    sentiment: 'positive',
  },
  {
    id: 's-2',
    start: Date.now() - 3600000 * 24 * 1 - 3600000 * 1,
    end: Date.now() - 3600000 * 24 * 1,
    purpose: 'relax',
    plannedMin: 15,
    actualMin: 34,
    goal: 'Check photography feed',
    mood: 'tired',
    avgScore: 42,
    timeline: [],
    interventions: [{ step: 'nudge', arm: 'banner', accepted: false, at: Date.now() - 86400000 }],
    extensions: [{ reason: 'One more post', at: Date.now() - 86400000 }],
    exitLoops: 2,
    achieved: false,
    regret: 7,
    journal: 'Got sucked into infinite scrolling late at night.',
    sentiment: 'negative',
  },
  {
    id: 's-3',
    start: Date.now() - 3600000 * 5,
    end: Date.now() - 3600000 * 4.6,
    purpose: 'quick',
    plannedMin: 10,
    actualMin: 11,
    goal: 'Reply to messages',
    mood: 'calm',
    avgScore: 78,
    timeline: [],
    interventions: [],
    extensions: [],
    exitLoops: 0,
    achieved: true,
    regret: 0,
    journal: 'Quick and intentional.',
    sentiment: 'positive',
  },
];

const INITIAL_APP_LIMITS: AppLimit[] = [
  {
    packageName: 'com.instagram.android',
    appName: 'Instagram',
    icon: '📸',
    category: 'Social',
    usedMinutesToday: 26,
    dailyLimitMinutes: 30,
    warningCount: 1,
    isBlocked: false,
    lastWarningTime: Date.now() - 180000,
  },
  {
    packageName: 'com.google.android.youtube',
    appName: 'YouTube',
    icon: '▶️',
    category: 'Video',
    usedMinutesToday: 38,
    dailyLimitMinutes: 45,
    warningCount: 0,
    isBlocked: false,
    lastWarningTime: 0,
  },
  {
    packageName: 'com.zhiliaoapp.musically',
    appName: 'TikTok',
    icon: '🎵',
    category: 'Short Video',
    usedMinutesToday: 21,
    dailyLimitMinutes: 20,
    warningCount: 2,
    isBlocked: true,
    lastWarningTime: Date.now() - 60000,
  },
  {
    packageName: 'com.twitter.android',
    appName: 'X (Twitter)',
    icon: '💬',
    category: 'Social',
    usedMinutesToday: 14,
    dailyLimitMinutes: 25,
    warningCount: 0,
    isBlocked: false,
    lastWarningTime: 0,
  },
  {
    packageName: 'com.reddit.frontpage',
    appName: 'Reddit',
    icon: '🤖',
    category: 'Forums',
    usedMinutesToday: 18,
    dailyLimitMinutes: 30,
    warningCount: 0,
    isBlocked: false,
    lastWarningTime: 0,
  },
];

export default function App() {
  // Top level view mode: phone simulator, android code inspector, or test runner
  const [activeTab, setActiveTab] = useState<'phone' | 'inspector' | 'tests'>('phone');

  // Phone screen navigation: today | intent | session | recap | insights | lab | coach | focus | settings
  const [currentScreen, setCurrentScreen] = useState<
    'today' | 'intent' | 'session' | 'recap' | 'insights' | 'lab' | 'coach' | 'focus' | 'settings'
  >('today');

  // App State
  const [sessions, setSessions] = useState<SessionRecord[]>(() => {
    const saved = localStorage.getItem('enak_sessions');
    return saved ? JSON.parse(saved) : INITIAL_SESSIONS;
  });

  const [settings, setSettings] = useState<UserSettings>(() => {
    const saved = localStorage.getItem('enak_settings');
    return saved
      ? JSON.parse(saved)
      : {
          name: 'Traveler',
          personality: 'calm',
          language: 'en',
          weeklyGoalHours: 14,
          quietHoursStart: 22,
          quietHoursEnd: 7,
          gentleMode: false,
          sleepGuard: true,
          isOnboarded: true,
        };
  });

  const [appLimits, setAppLimits] = useState<AppLimit[]>(() => {
    const saved = localStorage.getItem('enak_limits');
    return saved ? JSON.parse(saved) : INITIAL_APP_LIMITS;
  });

  const [banditStats, setBanditStats] = useState<ArmStats>(() => {
    const saved = localStorage.getItem('enak_bandit');
    return saved ? JSON.parse(saved) : emptyStats();
  });

  // Save changes to localStorage
  useEffect(() => {
    localStorage.setItem('enak_sessions', JSON.stringify(sessions));
  }, [sessions]);

  useEffect(() => {
    localStorage.setItem('enak_settings', JSON.stringify(settings));
  }, [settings]);

  useEffect(() => {
    localStorage.setItem('enak_limits', JSON.stringify(appLimits));
  }, [appLimits]);

  useEffect(() => {
    localStorage.setItem('enak_bandit', JSON.stringify(banditStats));
  }, [banditStats]);

  // Intent form state
  const [intentPurpose, setIntentPurpose] = useState<Purpose>('learn');
  const [intentDuration, setIntentDuration] = useState<number>(15);
  const [intentGoal, setIntentGoal] = useState<string>('Learn AI fundamentals in 15m');
  const [intentMood, setIntentMood] = useState<Mood>('curious');
  const [selectedVideo, setSelectedVideo] = useState<string | null>(LEARNING_VIDEOS[0].id);

  // Live session state
  const [sessionActive, setSessionActive] = useState<boolean>(false);
  const [elapsedSec, setElapsedSec] = useState<number>(0);
  const [sessionSignals, setSessionSignals] = useState<Signals>({
    scrollVelocity: 200,
    flickRate: 2,
    avgDwell: 8,
    refreshRate: 0.5,
    purposeMatch: 0.9,
    hour: new Date().getHours(),
    baselineRatio: 1.0,
  });

  // Warnings & intervention overlay state
  const [warningCount, setWarningCount] = useState<number>(0);
  const [lastWarningTime, setLastWarningTime] = useState<number>(0);
  const [activeIntervention, setActiveIntervention] = useState<{
    step: Step;
    arm: Arm;
    isWarning: boolean;
  } | null>(null);
  const [shownLadderSteps, setShownLadderSteps] = useState<number>(-1);
  const [activeBlocker, setActiveBlocker] = useState<{
    app: AppLimit;
    isHardBlock: boolean;
  } | null>(null);

  // Completed session for recap
  const [completedSession, setCompletedSession] = useState<SessionRecord | null>(null);
  const [recapRegret, setRecapRegret] = useState<number>(2);
  const [recapAchieved, setRecapAchieved] = useState<boolean>(true);
  const [recapJournal, setRecapJournal] = useState<string>('');
  const [journalSentiment, setJournalSentiment] = useState<'positive' | 'neutral' | 'negative' | null>(null);
  const [analyzingJournal, setAnalyzingJournal] = useState<boolean>(false);

  // AI Coach chat state
  const [coachMessages, setCoachMessages] = useState<CoachMessage[]>([
    {
      id: 'm-init',
      role: 'assistant',
      content:
        "Welcome to Enak. I'm your mindful screen time coach. I'm here to support what you set out to do—without judgment. How can I help you stay deliberate today?",
      timestamp: Date.now(),
    },
  ]);
  const [coachInput, setCoachInput] = useState<string>('');
  const [isCoachThinking, setIsCoachThinking] = useState<boolean>(false);
  const [isListening, setIsListening] = useState<boolean>(false);
  const [isSpeechEnabled, setIsSpeechEnabled] = useState<boolean>(true);

  // TTS Helper
  const speakText = (text: string) => {
    if (!isSpeechEnabled || typeof window === 'undefined' || !('speechSynthesis' in window)) return;
    try {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 1.0;
      utterance.pitch = 1.0;
      window.speechSynthesis.speak(utterance);
    } catch (e) {
      console.warn('TTS error', e);
    }
  };

  // Vibrate Helper
  const triggerVibrate = () => {
    if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
      try {
        navigator.vibrate([150, 80, 150]);
      } catch (e) {
        // Ignored
      }
    }
  };

  // Live Timer tick
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (sessionActive) {
      interval = setInterval(() => {
        setElapsedSec((prev) => {
          const next = prev + 1;

          // Check warnings
          const plannedMin = intentDuration;
          const { score, label } = computeScore(sessionSignals);
          const isDrifting = label === 'Compulsive';
          const now = Date.now();

          // Two warnings rule: at plan and 1 min later, or on drift
          if (warningDue(next, plannedMin, warningCount, lastWarningTime, now, isDrifting)) {
            const newCount = warningCount + 1;
            setWarningCount(newCount);
            setLastWarningTime(now);
            triggerVibrate();
            speakText(`Enak warning ${newCount} of 2. You have reached your planned boundary.`);
            setActiveIntervention({
              step: 'reflect',
              arm: pickArm(banditStats),
              isWarning: true,
            });
          }

          // Evaluate Intervention ladder
          const overrun = overrunState(next, plannedMin);
          const nextLadderStep = nextStep({
            overrun,
            label,
            exitLoop: false,
            shown: shownLadderSteps,
            sleepGuard: settings.sleepGuard,
            gentleMode: settings.gentleMode,
          });

          if (nextLadderStep && !activeIntervention) {
            const arm = pickArm(banditStats);
            const stepIndex = ['nudge', 'reflect', 'friction', 'redirect', 'extend'].indexOf(nextLadderStep);
            setShownLadderSteps(Math.max(shownLadderSteps, stepIndex));
            setActiveIntervention({
              step: nextLadderStep,
              arm,
              isWarning: false,
            });
          }

          return next;
        });
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [
    sessionActive,
    intentDuration,
    warningCount,
    lastWarningTime,
    sessionSignals,
    shownLadderSteps,
    settings,
    banditStats,
    activeIntervention,
  ]);

  // Start Session
  const handleStartSession = () => {
    setElapsedSec(0);
    setWarningCount(0);
    setLastWarningTime(0);
    setShownLadderSteps(-1);
    setActiveIntervention(null);
    setSessionActive(true);
    setCurrentScreen('session');
  };

  // Finish Session & Go to Recap
  const handleFinishSession = () => {
    setSessionActive(false);
    const scoreObj = computeScore(sessionSignals);
    const newSession: SessionRecord = {
      id: `session-${Date.now()}`,
      start: Date.now() - elapsedSec * 1000,
      end: Date.now(),
      purpose: intentPurpose,
      plannedMin: intentDuration,
      actualMin: Math.max(1, Math.round(elapsedSec / 60)),
      goal: intentGoal,
      mood: intentMood,
      avgScore: scoreObj.score,
      timeline: [],
      interventions: [],
      extensions: [],
      exitLoops: 0,
      achieved: true,
      regret: 1,
      journal: '',
      sentiment: 'positive',
    };

    setCompletedSession(newSession);
    setSessions((prev) => [newSession, ...prev]);
    setCurrentScreen('recap');

    // Fire Confetti!
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.6 },
      colors: ['#8A2BE2', '#22E3F0', '#F3F4F6'],
    });
  };

  // Save Recap
  const handleSaveRecap = () => {
    if (!completedSession) return;
    const updated = {
      ...completedSession,
      achieved: recapAchieved,
      regret: recapRegret,
      journal: recapJournal,
      sentiment: journalSentiment || 'positive',
    };

    setSessions((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
    setCurrentScreen('today');
  };

  // Ask AI to analyze journal sentiment
  const handleAnalyzeJournal = async () => {
    if (!recapJournal.trim()) return;
    setAnalyzingJournal(true);
    try {
      const res = await fetch('/api/journal', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: recapJournal }),
      });
      const data = await res.json();
      setJournalSentiment(data.sentiment || 'positive');
    } catch (e) {
      setJournalSentiment('positive');
    } finally {
      setAnalyzingJournal(false);
    }
  };

  // Coach AI Message Send
  const handleSendCoachMessage = async (customPrompt?: string) => {
    const textToSend = customPrompt || coachInput;
    if (!textToSend.trim() || isCoachThinking) return;

    const userMsg: CoachMessage = {
      id: `usr-${Date.now()}`,
      role: 'user',
      content: textToSend,
      timestamp: Date.now(),
    };

    const newMessages = [...coachMessages, userMsg];
    setCoachMessages(newMessages);
    setCoachInput('');
    setIsCoachThinking(true);

    try {
      const statsSummary = `User completed ${sessions.length} sessions. Current streak: ${streak(
        sessions.map((s) => s.start),
        Date.now(),
      )} days. Latest goal: "${intentGoal}". Favorite purpose: ${intentPurpose}.`;

      const res = await fetch('/api/coach', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          messages: newMessages.map((m) => ({ role: m.role, content: m.content })),
          summary: statsSummary,
          language: settings.language,
          personality: settings.personality,
        }),
      });

      const data = await res.json();
      const replyText = data.text || "I'm right here with you. What feels most important to focus on next?";

      const aiMsg: CoachMessage = {
        id: `ai-${Date.now()}`,
        role: 'assistant',
        content: replyText,
        timestamp: Date.now(),
      };

      setCoachMessages([...newMessages, aiMsg]);
      speakText(replyText);
    } catch (e) {
      const fallbackMsg: CoachMessage = {
        id: `ai-${Date.now()}`,
        role: 'assistant',
        content:
          'Take a slow breath. Notice where you are and what you wanted to do. You are in control of your device.',
        timestamp: Date.now(),
      };
      setCoachMessages([...newMessages, fallbackMsg]);
    } finally {
      setIsCoachThinking(false);
    }
  };

  // Voice Input SpeechRecognition
  const handleToggleVoice = () => {
    if (typeof window === 'undefined') return;
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    if (isListening) {
      setIsListening(false);
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.lang = settings.language === 'hi' ? 'hi-IN' : 'en-US';
      recognition.continuous = false;
      recognition.interimResults = false;

      recognition.onstart = () => setIsListening(true);
      recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        setCoachInput(transcript);
        setIsListening(false);
      };
      recognition.onerror = () => setIsListening(false);
      recognition.onend = () => setIsListening(false);

      recognition.start();
    } catch (e) {
      setIsListening(false);
    }
  };

  // AI 5-Minute Negotiation API Call
  const handleNegotiateExtension = async (reason: string) => {
    try {
      const res = await fetch('/api/negotiate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reason,
          purpose: intentPurpose,
          goal: intentGoal,
          elapsedMin: Math.round(elapsedSec / 60),
          plannedMin: intentDuration,
        }),
      });
      const data = await res.json();
      if (data.accepted) {
        // Record win in Bandit stats
        if (activeIntervention?.arm) {
          setBanditStats((prev) => record(prev, activeIntervention.arm, true));
        }
      }
      return {
        accepted: Boolean(data.accepted),
        message: data.message || (data.accepted ? 'Extension granted.' : 'Challenge accepted.'),
      };
    } catch (e) {
      return {
        accepted: reason.length > 15,
        message: reason.length > 15 ? 'Extension granted.' : 'Please take a 20s breather instead.',
      };
    }
  };

  // Statistics Computations
  const userMetrics = metrics(sessions);
  const currentStreak = streak(
    sessions.map((s) => s.start),
    Date.now(),
  );

  const todaySessions = sessions.filter((s) => {
    const d = new Date(s.start);
    const now = new Date();
    return d.toDateString() === now.toDateString();
  });
  const todayMinutes = todaySessions.reduce((acc, s) => acc + s.actualMin, 0);

  // Intentional score for current session
  const currentScoreObj = computeScore(sessionSignals);

  return (
    <div className="min-h-screen bg-[#0B0B10] text-slate-100 flex flex-col font-sans">
      {/* Top Header Bar */}
      <header className="border-b border-white/10 bg-[#0F0F17]/80 backdrop-blur-md sticky top-0 z-40 px-6 py-3.5 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-2xl bg-gradient-to-tr from-[#8A2BE2] to-[#22E3F0] p-0.5 shadow-lg shadow-[#8A2BE2]/20">
            <div className="w-full h-full bg-[#0B0B10] rounded-[14px] flex items-center justify-center">
              <span className="font-bold text-base bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] bg-clip-text text-transparent">
                E
              </span>
            </div>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold text-white tracking-tight">Enak</h1>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/30 font-semibold tracking-wide uppercase">
                Native Android &amp; Engine
              </span>
            </div>
            <p className="text-[11px] text-slate-400">Mindful Digital Wellbeing &amp; Screen Time Shield</p>
          </div>
        </div>

        {/* Mode Switcher Tabs */}
        <div className="flex items-center gap-2 bg-[#171724] p-1 rounded-2xl border border-white/5">
          <button
            onClick={() => setActiveTab('phone')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all ${
              activeTab === 'phone'
                ? 'bg-gradient-to-r from-[#8A2BE2] to-[#A855F7] text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>Interactive App</span>
          </button>

          <button
            onClick={() => setActiveTab('inspector')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all ${
              activeTab === 'inspector'
                ? 'bg-gradient-to-r from-[#8A2BE2] to-[#A855F7] text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Code2 className="w-3.5 h-3.5" />
            <span>Android Code (:engine)</span>
          </button>

          <button
            onClick={() => setActiveTab('tests')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all ${
              activeTab === 'tests'
                ? 'bg-gradient-to-r from-[#8A2BE2] to-[#A855F7] text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Unit Tests</span>
          </button>
        </div>

        {/* Live Streak Badge */}
        <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-2xl bg-[#1B1B28] border border-white/10 text-xs">
          <Flame className="w-4 h-4 text-orange-400 animate-pulse" />
          <span className="font-semibold text-white">{currentStreak} day streak</span>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6 flex flex-col items-center">
        {activeTab === 'inspector' && (
          <div className="w-full h-[760px]">
            <AndroidCodeInspector />
          </div>
        )}

        {activeTab === 'tests' && (
          <div className="w-full max-w-3xl">
            <EngineTestRunner />
          </div>
        )}

        {activeTab === 'phone' && (
          <div className="w-full flex flex-col lg:flex-row items-center lg:items-start justify-center gap-8">
            {/* Native Android Phone Frame */}
            <div className="relative w-full max-w-[390px] h-[780px] bg-[#000000] rounded-[52px] p-3 shadow-2xl ring-1 ring-white/15 shadow-purple-900/20 flex flex-col overflow-hidden">
              {/* Outer Bezel Accent */}
              <div className="w-full h-full bg-[#0B0B10] rounded-[44px] flex flex-col overflow-hidden relative border border-white/10">
                {/* Android Status Bar */}
                <div className="h-10 px-6 flex items-center justify-between text-[11px] text-slate-300 font-mono select-none bg-gradient-to-b from-black/80 to-transparent z-30">
                  <span>9:41</span>
                  {/* Camera hole punch */}
                  <div className="w-3.5 h-3.5 rounded-full bg-black ring-1 ring-white/10" />
                  <div className="flex items-center gap-1.5 text-[10px]">
                    <span>5G</span>
                    <div className="w-4 h-2 rounded-sm border border-slate-400 flex items-center p-0.5">
                      <div className="w-2.5 h-1 bg-white rounded-2xs" />
                    </div>
                  </div>
                </div>

                {/* In-Phone Screen Content */}
                <div className="flex-1 overflow-y-auto px-4 pb-20 pt-2 select-none">
                  {/* SCREEN 1: TODAY */}
                  {currentScreen === 'today' && (
                    <div className="space-y-4">
                      {/* Greeting Card */}
                      <div className="p-4 rounded-3xl bg-gradient-to-br from-[#1C182F] to-[#12121E] border border-white/10 relative overflow-hidden">
                        <div className="flex items-center justify-between mb-3">
                          <div>
                            <span className="text-[11px] uppercase tracking-wider text-cyan-300 font-semibold">
                              Good Day, {settings.name}
                            </span>
                            <h2 className="text-xl font-bold text-white tracking-tight">
                              Stay on purpose.
                            </h2>
                          </div>
                          <div className="w-10 h-10 rounded-2xl bg-[#8A2BE2]/20 border border-[#8A2BE2]/40 flex items-center justify-center text-cyan-300">
                            <Sparkles className="w-5 h-5" />
                          </div>
                        </div>

                        {/* Quick Stats Grid */}
                        <div className="grid grid-cols-2 gap-2 mt-4 text-xs">
                          <div className="p-3 rounded-2xl bg-black/40 border border-white/5">
                            <span className="text-slate-400 block text-[10px]">Today on purpose</span>
                            <span className="text-lg font-bold text-white mt-0.5 block font-mono">
                              {todayMinutes}m
                            </span>
                            <span className="text-[10px] text-cyan-400">
                              {todaySessions.length} sessions
                            </span>
                          </div>

                          <div className="p-3 rounded-2xl bg-black/40 border border-white/5">
                            <span className="text-slate-400 block text-[10px]">Current Streak</span>
                            <span className="text-lg font-bold text-orange-400 mt-0.5 block font-mono">
                              {currentStreak} days
                            </span>
                            <span className="text-[10px] text-slate-400">Intentional streak</span>
                          </div>
                        </div>

                        {/* Reality check equivalent */}
                        <div className="mt-3 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
                          <span>Today&apos;s focus equals:</span>
                          <span className="text-cyan-300 font-medium">
                            {realityCheck(todayMinutes || 15)}
                          </span>
                        </div>
                      </div>

                      {/* Primary CTA: Start Intent */}
                      <button
                        onClick={() => setCurrentScreen('intent')}
                        className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white font-semibold text-sm hover:brightness-110 transition-all shadow-lg shadow-[#8A2BE2]/25 flex items-center justify-center gap-2"
                      >
                        <Play className="w-4 h-4 fill-current" />
                        <span>Declare Intent &amp; Start Session</span>
                      </button>

                      {/* Quick Start Purposes */}
                      <div>
                        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-2 px-1">
                          Quick Purposes
                        </span>
                        <div className="grid grid-cols-2 gap-2">
                          {[
                            { key: 'learn', label: 'Learn', icon: BookOpen, min: 20 },
                            { key: 'relax', label: 'Relax', icon: Coffee, min: 15 },
                            { key: 'social', label: 'Social', icon: Users, min: 10 },
                            { key: 'research', label: 'Research', icon: Search, min: 25 },
                          ].map((p) => {
                            const IconComponent = p.icon;
                            return (
                              <button
                                key={p.key}
                                onClick={() => {
                                  setIntentPurpose(p.key as Purpose);
                                  setIntentDuration(p.min);
                                  setCurrentScreen('intent');
                                }}
                                className="p-3 rounded-2xl bg-[#14141F] hover:bg-[#1B1B2A] border border-white/5 transition-all text-left flex items-center gap-2.5"
                              >
                                <div className="w-7 h-7 rounded-xl bg-white/5 flex items-center justify-center text-cyan-300">
                                  <IconComponent className="w-3.5 h-3.5" />
                                </div>
                                <div>
                                  <span className="text-xs font-semibold text-white block">
                                    {p.label}
                                  </span>
                                  <span className="text-[10px] text-slate-500 font-mono">
                                    {p.min} min
                                  </span>
                                </div>
                              </button>
                            );
                          })}
                        </div>
                      </div>

                      {/* Risky Hour Banner */}
                      <div className="p-3.5 rounded-2xl bg-rose-950/20 border border-rose-500/20 text-xs flex items-center gap-3">
                        <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0" />
                        <div className="text-[11px] leading-tight">
                          <span className="text-rose-200 font-semibold block">
                            Late Night Risk Window (22h–02h)
                          </span>
                          <span className="text-slate-400">
                            Drift rate is 2.1x higher during these hours. Keep sessions under 15m.
                          </span>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* SCREEN 2: INTENT */}
                  {currentScreen === 'intent' && (
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <h2 className="text-lg font-bold text-white">Declare Your Intention</h2>
                        <button
                          onClick={() => setCurrentScreen('today')}
                          className="text-xs text-slate-400 hover:text-white"
                        >
                          Cancel
                        </button>
                      </div>

                      {/* Purpose selector */}
                      <div>
                        <label className="text-xs font-medium text-slate-400 block mb-2">
                          What is your purpose?
                        </label>
                        <div className="grid grid-cols-3 gap-1.5">
                          {(['learn', 'relax', 'social', 'research', 'quick'] as Purpose[]).map(
                            (p) => (
                              <button
                                key={p}
                                onClick={() => setIntentPurpose(p)}
                                className={`py-2 px-2.5 rounded-xl text-xs font-medium capitalize transition-all ${
                                  intentPurpose === p
                                    ? 'bg-[#8A2BE2] text-white font-semibold shadow-md shadow-[#8A2BE2]/30'
                                    : 'bg-[#151522] text-slate-400 hover:text-white'
                                }`}
                              >
                                {p}
                              </button>
                            ),
                          )}
                        </div>
                      </div>

                      {/* Duration selector */}
                      <div>
                        <div className="flex items-center justify-between text-xs mb-2">
                          <span className="text-slate-400">Planned Duration</span>
                          <span className="font-mono text-cyan-300 font-semibold">
                            {intentDuration} minutes
                          </span>
                        </div>
                        <input
                          type="range"
                          min="5"
                          max="60"
                          step="5"
                          value={intentDuration}
                          onChange={(e) => setIntentDuration(Number(e.target.value))}
                          className="w-full accent-[#8A2BE2] h-1.5 bg-[#1F1F30] rounded-lg cursor-pointer"
                        />
                        <div className="flex justify-between text-[10px] text-slate-500 font-mono mt-1">
                          <span>5m</span>
                          <span>15m</span>
                          <span>30m</span>
                          <span>60m</span>
                        </div>
                      </div>

                      {/* Goal Input */}
                      <div>
                        <label className="text-xs font-medium text-slate-400 block mb-1.5">
                          What will you achieve? (Optional)
                        </label>
                        <input
                          type="text"
                          value={intentGoal}
                          onChange={(e) => setIntentGoal(e.target.value)}
                          placeholder="e.g., Read documentation on Jetpack Compose"
                          className="w-full px-3.5 py-2.5 rounded-xl bg-[#141420] border border-white/10 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-400"
                        />
                      </div>

                      {/* Mood Selector */}
                      <div>
                        <label className="text-xs font-medium text-slate-400 block mb-2">
                          Current mindset / mood:
                        </label>
                        <div className="flex flex-wrap gap-1.5">
                          {(['calm', 'curious', 'tired', 'stressed', 'bored'] as Mood[]).map((m) => (
                            <button
                              key={m}
                              onClick={() => setIntentMood(m)}
                              className={`px-3 py-1.5 rounded-xl text-xs capitalize transition-all ${
                                intentMood === m
                                  ? 'bg-[#22E3F0]/20 border border-[#22E3F0] text-cyan-300 font-semibold'
                                  : 'bg-[#141420] text-slate-400'
                              }`}
                            >
                              {m}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Optional Learning Video */}
                      <div className="p-3 rounded-2xl bg-[#12121E] border border-white/5 space-y-2">
                        <span className="text-[11px] font-semibold text-slate-300 block">
                          Optional Intentional Video Resource
                        </span>
                        <div className="space-y-1.5">
                          {LEARNING_VIDEOS.map((v) => (
                            <div
                              key={v.id}
                              onClick={() =>
                                setSelectedVideo(selectedVideo === v.id ? null : v.id)
                              }
                              className={`p-2 rounded-xl text-xs flex items-center justify-between cursor-pointer transition-colors ${
                                selectedVideo === v.id
                                  ? 'bg-[#8A2BE2]/20 border border-[#8A2BE2]/50 text-white'
                                  : 'bg-[#181826] text-slate-400 hover:text-slate-300'
                              }`}
                            >
                              <div className="truncate pr-2">
                                <span className="font-medium truncate block">{v.title}</span>
                                <span className="text-[10px] text-slate-500">{v.category}</span>
                              </div>
                              {selectedVideo === v.id && (
                                <CheckCircle2 className="w-4 h-4 text-cyan-400 shrink-0" />
                              )}
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Start Button */}
                      <button
                        onClick={handleStartSession}
                        className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white font-semibold text-sm hover:brightness-110 transition-all shadow-lg shadow-[#8A2BE2]/25"
                      >
                        Begin Session ({intentDuration}m)
                      </button>
                    </div>
                  )}

                  {/* SCREEN 3: LIVE SESSION */}
                  {currentScreen === 'session' && (
                    <div className="space-y-4">
                      {/* Top Session Status */}
                      <div className="flex items-center justify-between text-xs">
                        <div className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                          <span className="font-semibold text-white capitalize">
                            {intentPurpose} Session
                          </span>
                        </div>
                        <span className="text-slate-400 text-[11px]">
                          Goal: {intentGoal ? `"${intentGoal.slice(0, 18)}..."` : 'Focused'}
                        </span>
                      </div>

                      {/* Dual Ring Live Timer */}
                      <div className="py-2 flex justify-center">
                        <DualRingTimer
                          elapsedSec={elapsedSec}
                          plannedSec={intentDuration * 60}
                          score={currentScoreObj.score}
                          label={currentScoreObj.label}
                          size={220}
                        />
                      </div>

                      {/* Interactive Gesture Simulator Controls */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-3">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-semibold text-slate-300 flex items-center gap-1.5">
                            <Sliders className="w-3.5 h-3.5 text-cyan-400" />
                            Behavior &amp; Drift Simulator
                          </span>
                          <span className="text-[10px] text-slate-500">Live Engine Inputs</span>
                        </div>

                        {/* Scroll velocity */}
                        <div className="space-y-1">
                          <div className="flex justify-between text-[11px] text-slate-400">
                            <span>Scroll Speed</span>
                            <span className="font-mono text-white">
                              {sessionSignals.scrollVelocity} px/s
                            </span>
                          </div>
                          <input
                            type="range"
                            min="50"
                            max="2500"
                            step="50"
                            value={sessionSignals.scrollVelocity}
                            onChange={(e) =>
                              setSessionSignals((prev: Signals) => ({
                                ...prev,
                                scrollVelocity: Number(e.target.value),
                              }))
                            }
                            className="w-full h-1 bg-[#1F1F30] rounded-lg accent-[#8A2BE2]"
                          />
                        </div>

                        {/* Flick Rate */}
                        <div className="space-y-1">
                          <div className="flex justify-between text-[11px] text-slate-400">
                            <span>Flicks per minute (&lt;1.2s skips)</span>
                            <span className="font-mono text-white">
                              {sessionSignals.flickRate}
                            </span>
                          </div>
                          <input
                            type="range"
                            min="0"
                            max="30"
                            step="1"
                            value={sessionSignals.flickRate}
                            onChange={(e) =>
                              setSessionSignals((prev: Signals) => ({
                                ...prev,
                                flickRate: Number(e.target.value),
                              }))
                            }
                            className="w-full h-1 bg-[#1F1F30] rounded-lg accent-[#8A2BE2]"
                          />
                        </div>

                        {/* Dwell time */}
                        <div className="space-y-1">
                          <div className="flex justify-between text-[11px] text-slate-400">
                            <span>Avg Item Dwell Time</span>
                            <span className="font-mono text-white">
                              {sessionSignals.avgDwell}s
                            </span>
                          </div>
                          <input
                            type="range"
                            min="0.5"
                            max="15"
                            step="0.5"
                            value={sessionSignals.avgDwell}
                            onChange={(e) =>
                              setSessionSignals((prev: Signals) => ({
                                ...prev,
                                avgDwell: Number(e.target.value),
                              }))
                            }
                            className="w-full h-1 bg-[#1F1F30] rounded-lg accent-[#22E3F0]"
                          />
                        </div>

                        {/* Purpose match */}
                        <div className="space-y-1">
                          <div className="flex justify-between text-[11px] text-slate-400">
                            <span>Content Purpose Match</span>
                            <span className="font-mono text-white">
                              {Math.round(sessionSignals.purposeMatch * 100)}%
                            </span>
                          </div>
                          <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.05"
                            value={sessionSignals.purposeMatch}
                            onChange={(e) =>
                              setSessionSignals((prev: Signals) => ({
                                ...prev,
                                purposeMatch: Number(e.target.value),
                              }))
                            }
                            className="w-full h-1 bg-[#1F1F30] rounded-lg accent-[#22E3F0]"
                          />
                        </div>
                      </div>

                      {/* Trigger Intervention Test buttons */}
                      <div className="flex gap-2">
                        <button
                          onClick={() => {
                            setActiveIntervention({
                              step: 'friction',
                              arm: 'pause',
                              isWarning: false,
                            });
                          }}
                          className="flex-1 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-[11px] text-cyan-300 font-medium"
                        >
                          Simulate Breather
                        </button>
                        <button
                          onClick={() => {
                            setActiveIntervention({
                              step: 'extend',
                              arm: 'question',
                              isWarning: false,
                            });
                          }}
                          className="flex-1 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-[11px] text-purple-300 font-medium"
                        >
                          Simulate +5m AI
                        </button>
                      </div>

                      {/* Finish Session CTA */}
                      <button
                        onClick={handleFinishSession}
                        className="w-full py-3 rounded-2xl bg-white/10 hover:bg-white/15 text-white font-medium text-xs transition-colors"
                      >
                        Wrap Up &amp; Record Reflections
                      </button>
                    </div>
                  )}

                  {/* SCREEN 4: RECAP */}
                  {currentScreen === 'recap' && completedSession && (
                    <div className="space-y-4">
                      <div className="text-center py-2">
                        <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 mx-auto flex items-center justify-center mb-2">
                          <CheckCircle2 className="w-7 h-7" />
                        </div>
                        <h2 className="text-lg font-bold text-white">Session Completed</h2>
                        <p className="text-xs text-slate-400">
                          {completedSession.actualMin}m on{' '}
                          <span className="capitalize">{completedSession.purpose}</span>
                        </p>
                      </div>

                      {/* Goal achievement toggle */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 flex items-center justify-between">
                        <div>
                          <span className="text-xs font-semibold text-white block">
                            Did you achieve what you came for?
                          </span>
                          <span className="text-[11px] text-slate-400">
                            {completedSession.goal || 'On-purpose session'}
                          </span>
                        </div>
                        <div className="flex gap-1.5">
                          <button
                            onClick={() => setRecapAchieved(true)}
                            className={`px-3 py-1 rounded-xl text-xs font-semibold ${
                              recapAchieved
                                ? 'bg-emerald-500 text-white'
                                : 'bg-white/5 text-slate-400'
                            }`}
                          >
                            Yes
                          </button>
                          <button
                            onClick={() => setRecapAchieved(false)}
                            className={`px-3 py-1 rounded-xl text-xs font-semibold ${
                              !recapAchieved
                                ? 'bg-rose-500 text-white'
                                : 'bg-white/5 text-slate-400'
                            }`}
                          >
                            No
                          </button>
                        </div>
                      </div>

                      {/* Regret Slider (0-10) */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-2">
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-slate-300 font-semibold">Post-Session Regret</span>
                          <span className="font-mono text-cyan-300 font-bold">
                            {recapRegret} / 10
                          </span>
                        </div>
                        <input
                          type="range"
                          min="0"
                          max="10"
                          value={recapRegret}
                          onChange={(e) => setRecapRegret(Number(e.target.value))}
                          className="w-full h-1.5 bg-[#1F1F30] rounded-lg accent-[#8A2BE2]"
                        />
                        <div className="flex justify-between text-[10px] text-slate-500">
                          <span>0 (Delightful)</span>
                          <span>5 (Neutral)</span>
                          <span>10 (Heavy Regret)</span>
                        </div>
                      </div>

                      {/* Reflection Journal */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-2">
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-slate-300 font-semibold">
                            Mindful Reflections Journal
                          </span>
                          {journalSentiment && (
                            <span
                              className={`text-[10px] px-2 py-0.5 rounded-full capitalize font-semibold ${
                                journalSentiment === 'positive'
                                  ? 'bg-emerald-500/20 text-emerald-300'
                                  : journalSentiment === 'negative'
                                  ? 'bg-rose-500/20 text-rose-300'
                                  : 'bg-slate-500/20 text-slate-300'
                              }`}
                            >
                              {journalSentiment} sentiment
                            </span>
                          )}
                        </div>

                        <textarea
                          value={recapJournal}
                          onChange={(e) => setRecapJournal(e.target.value)}
                          placeholder="How did this session feel? What distracted you or helped you?"
                          rows={3}
                          className="w-full p-2.5 rounded-xl bg-[#0B0B10] border border-white/10 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-400 resize-none"
                        />

                        <button
                          onClick={handleAnalyzeJournal}
                          disabled={analyzingJournal || !recapJournal.trim()}
                          className="w-full py-1.5 rounded-xl bg-white/5 hover:bg-white/10 text-[11px] text-cyan-300 font-medium disabled:opacity-40"
                        >
                          {analyzingJournal
                            ? 'Classifying Sentiment with Gemini...'
                            : 'Tag Sentiment with Gemini AI'}
                        </button>
                      </div>

                      {/* Save Button */}
                      <button
                        onClick={handleSaveRecap}
                        className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white font-semibold text-sm hover:brightness-110 shadow-lg shadow-[#8A2BE2]/25"
                      >
                        Save &amp; Return to Dashboard
                      </button>
                    </div>
                  )}

                  {/* SCREEN 5: INSIGHTS */}
                  {currentScreen === 'insights' && (
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <h2 className="text-base font-bold text-white">Mindful Patterns &amp; Risk</h2>
                        <span className="text-[11px] text-cyan-300 font-mono">
                          {sessions.length} sessions
                        </span>
                      </div>

                      {/* Top Metrics Cards */}
                      <div className="grid grid-cols-2 gap-2 text-xs">
                        <div className="p-3 rounded-2xl bg-[#141420] border border-white/5">
                          <span className="text-[10px] text-slate-400 block">Overrun Rate</span>
                          <span className="text-base font-bold text-white font-mono mt-0.5 block">
                            {Math.round(userMetrics.overrunRate * 100)}%
                          </span>
                        </div>
                        <div className="p-3 rounded-2xl bg-[#141420] border border-white/5">
                          <span className="text-[10px] text-slate-400 block">Avg Regret Score</span>
                          <span className="text-base font-bold text-cyan-300 font-mono mt-0.5 block">
                            {userMetrics.avgRegret.toFixed(1)} / 10
                          </span>
                        </div>
                      </div>

                      {/* 7x24 Heatmap Component */}
                      <HeatmapView sessions={sessions} />
                    </div>
                  )}

                  {/* SCREEN 6: LAB & GARDEN */}
                  {currentScreen === 'lab' && (
                    <div className="space-y-4">
                      <div>
                        <h2 className="text-base font-bold text-white">The Enak Lab</h2>
                        <p className="text-xs text-slate-400">
                          Thompson-sampling experiments &amp; living mindful garden
                        </p>
                      </div>

                      {/* Mindful Garden */}
                      <div className="p-4 rounded-3xl bg-gradient-to-b from-[#131E19] to-[#0E1513] border border-emerald-500/20 text-center relative overflow-hidden">
                        <span className="text-[10px] font-semibold text-emerald-400 uppercase tracking-widest block mb-2">
                          Your Living Garden
                        </span>

                        <div className="w-24 h-24 mx-auto my-2 flex items-center justify-center relative">
                          <Leaf
                            className="w-16 h-16 text-emerald-400 animate-pulse transition-transform"
                            style={{
                              transform: `scale(${Math.min(1.4, 0.8 + sessions.length * 0.1)})`,
                            }}
                          />
                          <div className="absolute inset-0 bg-emerald-400/20 blur-xl rounded-full" />
                        </div>

                        <h3 className="text-sm font-bold text-white mt-1">
                          Flourishing Bonsai (Level {Math.max(1, Math.floor(sessions.length / 2))})
                        </h3>
                        <p className="text-[11px] text-slate-400 mt-1">
                          Grows with every intentional session completed without regret.
                        </p>
                      </div>

                      {/* Bandit Experimentation */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-3">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-semibold text-slate-300">
                            Intervention Bandit (Thompson Sampling)
                          </span>
                          <span className="text-[10px] text-slate-500">Learns what works</span>
                        </div>

                        <div className="space-y-2">
                          {(['banner', 'question', 'pause', 'suggestion'] as Arm[]).map((arm) => {
                            const stat = banditStats[arm] || { wins: 0, trials: 0 };
                            const rate = Math.round(successRate(stat) * 100);
                            return (
                              <div
                                key={arm}
                                className="p-2 rounded-xl bg-[#0F0F18] border border-white/5 text-xs flex items-center justify-between"
                              >
                                <div>
                                  <span className="font-medium text-white block">
                                    {ARM_NAMES[arm]}
                                  </span>
                                  <span className="text-[10px] text-slate-500 font-mono">
                                    {stat.wins} wins / {stat.trials} trials
                                  </span>
                                </div>
                                <span className="font-mono text-cyan-300 font-bold">{rate}%</span>
                              </div>
                            );
                          })}
                        </div>

                        <button
                          onClick={() => {
                            // Simulate 5 bandit trials
                            let s = { ...banditStats };
                            for (let i = 0; i < 5; i++) {
                              const arm = pickArm(s);
                              // Pauses and questions have slightly higher organic conversion
                              const win = arm === 'pause' || arm === 'question' ? Math.random() > 0.3 : Math.random() > 0.6;
                              s = record(s, arm, win);
                            }
                            setBanditStats(s);
                          }}
                          className="w-full py-2 rounded-xl bg-white/5 hover:bg-white/10 text-xs text-purple-300 font-medium"
                        >
                          Simulate 5 Interventions
                        </button>
                      </div>

                      {/* Buddy Code */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 flex items-center justify-between text-xs">
                        <div>
                          <span className="text-slate-300 font-semibold block">Accountability Buddy</span>
                          <span className="text-[10px] text-slate-500">
                            Share weekly code: <strong className="text-cyan-300">ENAK-49X8</strong>
                          </span>
                        </div>
                        <button
                          onClick={() => {
                            navigator.clipboard.writeText('ENAK-49X8');
                            alert('Buddy code copied to clipboard!');
                          }}
                          className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-cyan-300"
                        >
                          <Share2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  )}

                  {/* SCREEN 7: COACH */}
                  {currentScreen === 'coach' && (
                    <div className="h-[620px] flex flex-col justify-between">
                      {/* Coach Header Controls */}
                      <div className="p-3 rounded-2xl bg-[#141420] border border-white/5 mb-3 flex items-center justify-between text-xs">
                        <div className="flex items-center gap-2">
                          <Bot className="w-4 h-4 text-cyan-400" />
                          <span className="font-semibold text-white capitalize">
                            {settings.personality} Coach
                          </span>
                        </div>

                        <div className="flex items-center gap-1.5">
                          {/* Personality cycle */}
                          <button
                            onClick={() => {
                              const pList: Personality[] = ['calm', 'funny', 'nerd', 'strict'];
                              const nextP =
                                pList[(pList.indexOf(settings.personality) + 1) % pList.length];
                              setSettings((prev) => ({ ...prev, personality: nextP }));
                            }}
                            className="px-2 py-0.5 rounded-md bg-white/5 text-[10px] text-slate-300 font-mono capitalize"
                          >
                            {settings.personality}
                          </button>

                          {/* Language cycle */}
                          <button
                            onClick={() => {
                              const lList: Array<'en' | 'hinglish' | 'hi'> = ['en', 'hinglish', 'hi'];
                              const nextL =
                                lList[(lList.indexOf(settings.language) + 1) % lList.length];
                              setSettings((prev) => ({ ...prev, language: nextL }));
                            }}
                            className="px-2 py-0.5 rounded-md bg-white/5 text-[10px] text-cyan-300 font-mono uppercase"
                          >
                            {settings.language}
                          </button>

                          {/* Speech Audio toggle */}
                          <button
                            onClick={() => setIsSpeechEnabled(!isSpeechEnabled)}
                            className="p-1 text-slate-400 hover:text-white"
                          >
                            {isSpeechEnabled ? (
                              <Volume2 className="w-3.5 h-3.5 text-cyan-400" />
                            ) : (
                              <VolumeX className="w-3.5 h-3.5" />
                            )}
                          </button>
                        </div>
                      </div>

                      {/* Chat Messages */}
                      <div className="flex-1 overflow-y-auto space-y-2.5 pr-1 mb-3 text-xs">
                        {coachMessages.map((msg) => (
                          <div
                            key={msg.id}
                            className={`flex ${
                              msg.role === 'user' ? 'justify-end' : 'justify-start'
                            }`}
                          >
                            <div
                              className={`max-w-[85%] p-3 rounded-2xl leading-relaxed ${
                                msg.role === 'user'
                                  ? 'bg-[#8A2BE2] text-white rounded-br-none shadow-md'
                                  : 'bg-[#181826] text-slate-200 border border-white/5 rounded-bl-none'
                              }`}
                            >
                              {msg.content}
                            </div>
                          </div>
                        ))}

                        {isCoachThinking && (
                          <div className="flex justify-start">
                            <div className="bg-[#181826] border border-white/5 p-3 rounded-2xl text-slate-400 text-xs flex items-center gap-2">
                              <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping" />
                              <span>Enak coach is reflecting...</span>
                            </div>
                          </div>
                        )}
                      </div>

                      {/* Quick Prompt Pills */}
                      <div className="flex gap-1.5 overflow-x-auto pb-2 mb-2 no-scrollbar text-[11px]">
                        {[
                          'Why do I drift late at night?',
                          'Analyze my intentionality today',
                          'Help me do a 1-minute reset',
                        ].map((promptText, i) => (
                          <button
                            key={i}
                            onClick={() => handleSendCoachMessage(promptText)}
                            className="whitespace-nowrap px-2.5 py-1 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 border border-white/5 shrink-0 transition-colors"
                          >
                            {promptText}
                          </button>
                        ))}
                      </div>

                      {/* Input Box */}
                      <div className="flex items-center gap-1.5 bg-[#141422] p-1.5 rounded-2xl border border-white/10">
                        <button
                          onClick={handleToggleVoice}
                          className={`p-2 rounded-xl transition-colors ${
                            isListening
                              ? 'bg-rose-500 text-white animate-pulse'
                              : 'text-slate-400 hover:text-white'
                          }`}
                          title="Voice input (Speech Recognition)"
                        >
                          {isListening ? (
                            <MicOff className="w-4 h-4" />
                          ) : (
                            <Mic className="w-4 h-4" />
                          )}
                        </button>

                        <input
                          type="text"
                          value={coachInput}
                          onChange={(e) => setCoachInput(e.target.value)}
                          onKeyDown={(e) => e.key === 'Enter' && handleSendCoachMessage()}
                          placeholder="Speak or type your reflection..."
                          className="flex-1 bg-transparent px-2 text-xs text-white placeholder-slate-500 focus:outline-none"
                        />

                        <button
                          onClick={() => handleSendCoachMessage()}
                          disabled={!coachInput.trim() || isCoachThinking}
                          className="p-2 rounded-xl bg-gradient-to-r from-[#8A2BE2] to-[#22E3F0] text-white disabled:opacity-40 transition-opacity"
                        >
                          <Send className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  )}

                  {/* SCREEN 8: FOCUS & PERMISSIONS */}
                  {currentScreen === 'focus' && (
                    <div className="space-y-4">
                      <div>
                        <h2 className="text-base font-bold text-white">Focus &amp; App Boundaries</h2>
                        <p className="text-xs text-slate-400">
                          Real-time device limits, blockers, &amp; permissions centre
                        </p>
                      </div>

                      {/* Permissions Status Summary */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-2">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-semibold text-slate-300">
                            Native Permissions Centre
                          </span>
                          <span className="text-[10px] text-emerald-400 font-mono">
                            6 of 6 Allowed
                          </span>
                        </div>

                        <div className="grid grid-cols-2 gap-1.5 text-[11px]">
                          {[
                            { name: 'Usage Stats', status: 'Allowed' },
                            { name: 'Overlay Shield', status: 'Allowed' },
                            { name: 'Notifications', status: 'Allowed' },
                            { name: 'Battery Bypass', status: 'Allowed' },
                          ].map((perm) => (
                            <div
                              key={perm.name}
                              className="p-2 rounded-xl bg-[#0F0F18] border border-white/5 flex items-center justify-between"
                            >
                              <span className="text-slate-400">{perm.name}</span>
                              <span className="text-emerald-400 text-[10px] font-semibold">
                                {perm.status}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Per-App Daily Limits */}
                      <div className="space-y-2">
                        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block px-1">
                          App Timers Today
                        </span>

                        {appLimits.map((app) => {
                          const status = limitStatus(app.usedMinutesToday, app.dailyLimitMinutes);
                          const isOver = status === 'over';
                          const isNear = status === 'near';
                          return (
                            <div
                              key={app.packageName}
                              className={`p-3 rounded-2xl border transition-all ${
                                isOver
                                  ? 'bg-rose-950/20 border-rose-500/30'
                                  : isNear
                                  ? 'bg-amber-950/20 border-amber-500/30'
                                  : 'bg-[#141420] border-white/5'
                              }`}
                            >
                              <div className="flex items-center justify-between mb-2">
                                <div className="flex items-center gap-2.5">
                                  <span className="text-xl">{app.icon}</span>
                                  <div>
                                    <span className="text-xs font-semibold text-white block">
                                      {app.appName}
                                    </span>
                                    <span className="text-[10px] text-slate-500">
                                      {app.category}
                                    </span>
                                  </div>
                                </div>

                                <div className="text-right">
                                  <span className="text-xs font-mono font-bold text-white block">
                                    {app.usedMinutesToday} / {app.dailyLimitMinutes}m
                                  </span>
                                  <span
                                    className={`text-[10px] font-semibold ${
                                      isOver
                                        ? 'text-rose-400'
                                        : isNear
                                        ? 'text-amber-400'
                                        : 'text-cyan-400'
                                    }`}
                                  >
                                    {isOver ? 'Limit reached' : isNear ? 'Near 80%' : 'On plan'}
                                  </span>
                                </div>
                              </div>

                              {/* Action buttons */}
                              <div className="flex gap-2 mt-2 pt-2 border-t border-white/5">
                                <button
                                  onClick={() => {
                                    setActiveBlocker({
                                      app,
                                      isHardBlock: isOver,
                                    });
                                  }}
                                  className="flex-1 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 text-[11px] text-slate-300 font-medium"
                                >
                                  Test Blocker Overlay
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>

                      {/* Quiet Hours */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 flex items-center justify-between text-xs">
                        <div>
                          <span className="font-semibold text-white block">Quiet Hours Shield</span>
                          <span className="text-[10px] text-slate-400">
                            {settings.quietHoursStart}:00 – {settings.quietHoursEnd}:00 (wraps past
                            midnight)
                          </span>
                        </div>
                        <span className="text-cyan-400 font-mono text-[11px] font-bold">
                          {inQuietHours(
                            new Date().getHours(),
                            settings.quietHoursStart,
                            settings.quietHoursEnd,
                          )
                            ? 'ACTIVE NOW'
                            : 'STANDBY'}
                        </span>
                      </div>
                    </div>
                  )}

                  {/* SCREEN 9: SETTINGS */}
                  {currentScreen === 'settings' && (
                    <div className="space-y-4">
                      <div>
                        <h2 className="text-base font-bold text-white">Settings &amp; Data</h2>
                        <p className="text-xs text-slate-400">Local-first privacy &amp; customization</p>
                      </div>

                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-3">
                        <span className="text-xs font-semibold text-slate-300 block">
                          Profile Preferences
                        </span>

                        <div className="space-y-1">
                          <label className="text-[11px] text-slate-400">Display Name</label>
                          <input
                            type="text"
                            value={settings.name}
                            onChange={(e) =>
                              setSettings((prev) => ({ ...prev, name: e.target.value }))
                            }
                            className="w-full px-3 py-2 rounded-xl bg-[#0B0B10] border border-white/10 text-xs text-white"
                          />
                        </div>

                        <div className="space-y-1">
                          <label className="text-[11px] text-slate-400">Weekly Goal Budget</label>
                          <div className="flex items-center gap-2">
                            <input
                              type="number"
                              min="5"
                              max="40"
                              value={settings.weeklyGoalHours}
                              onChange={(e) =>
                                setSettings((prev) => ({
                                  ...prev,
                                  weeklyGoalHours: Number(e.target.value),
                                }))
                              }
                              className="w-20 px-3 py-2 rounded-xl bg-[#0B0B10] border border-white/10 text-xs text-white"
                            />
                            <span className="text-xs text-slate-400">hours on purpose / week</span>
                          </div>
                        </div>

                        {/* Gentle Mode toggle */}
                        <div className="flex items-center justify-between pt-2 border-t border-white/5 text-xs">
                          <div>
                            <span className="text-white block font-medium">Gentle Mode</span>
                            <span className="text-[10px] text-slate-500">
                              Caps interventions at soft reflection
                            </span>
                          </div>
                          <input
                            type="checkbox"
                            checked={settings.gentleMode}
                            onChange={(e) =>
                              setSettings((prev) => ({ ...prev, gentleMode: e.target.checked }))
                            }
                            className="w-4 h-4 accent-[#8A2BE2]"
                          />
                        </div>

                        {/* Sleep Guard toggle */}
                        <div className="flex items-center justify-between text-xs">
                          <div>
                            <span className="text-white block font-medium">Sleep Guard</span>
                            <span className="text-[10px] text-slate-500">
                              Strengthens ladder steps past 23:00
                            </span>
                          </div>
                          <input
                            type="checkbox"
                            checked={settings.sleepGuard}
                            onChange={(e) =>
                              setSettings((prev) => ({ ...prev, sleepGuard: e.target.checked }))
                            }
                            className="w-4 h-4 accent-[#8A2BE2]"
                          />
                        </div>
                      </div>

                      {/* Export & Reset Data */}
                      <div className="p-3.5 rounded-2xl bg-[#141420] border border-white/5 space-y-2">
                        <span className="text-xs font-semibold text-slate-300 block">
                          Data &amp; Privacy (Local First)
                        </span>

                        <button
                          onClick={() => {
                            const dataStr =
                              'data:text/json;charset=utf-8,' +
                              encodeURIComponent(JSON.stringify({ sessions, settings, appLimits }));
                            const downloadAnchor = document.createElement('a');
                            downloadAnchor.setAttribute('href', dataStr);
                            downloadAnchor.setAttribute('download', 'enak-export.json');
                            document.body.appendChild(downloadAnchor);
                            downloadAnchor.click();
                            downloadAnchor.remove();
                          }}
                          className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-xs text-cyan-300 font-medium flex items-center justify-center gap-2"
                        >
                          <Download className="w-3.5 h-3.5" />
                          <span>Export Everything (JSON)</span>
                        </button>

                        <button
                          onClick={() => {
                            if (confirm('Erase all local sessions and reset to default?')) {
                              localStorage.clear();
                              setSessions(INITIAL_SESSIONS);
                              setAppLimits(INITIAL_APP_LIMITS);
                              setBanditStats(emptyStats());
                              alert('Local data cleared.');
                            }
                          }}
                          className="w-full py-2.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 text-xs font-medium flex items-center justify-center gap-2"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          <span>Delete Everything</span>
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                {/* Android Pill Bottom Bar */}
                <div className="absolute bottom-2 left-4 right-4 h-16 bg-[#161624]/90 backdrop-blur-lg border border-white/10 rounded-3xl px-3 flex items-center justify-around z-30">
                  {[
                    { id: 'today', label: 'Today', icon: Award },
                    { id: 'insights', label: 'Insights', icon: Clock },
                    { id: 'coach', label: 'Coach', icon: Bot },
                    { id: 'focus', label: 'Focus', icon: Shield },
                    { id: 'lab', label: 'Lab', icon: Leaf },
                  ].map((tab) => {
                    const IconComponent = tab.icon;
                    const isSelected = currentScreen === tab.id;
                    return (
                      <button
                        key={tab.id}
                        onClick={() => {
                          if (sessionActive && currentScreen === 'session') {
                            if (!confirm('Leave active session? Timer will continue.')) return;
                          }
                          setCurrentScreen(tab.id as any);
                        }}
                        className={`flex flex-col items-center justify-center w-12 h-12 rounded-2xl transition-all ${
                          isSelected
                            ? 'text-cyan-300 font-semibold'
                            : 'text-slate-400 hover:text-slate-200'
                        }`}
                      >
                        <IconComponent
                          className={`w-4 h-4 transition-transform ${
                            isSelected ? 'scale-110 text-[#8A2BE2]' : ''
                          }`}
                        />
                        <span className="text-[10px] mt-0.5 tracking-tight">{tab.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

            {/* Side Companion Panel: Overview, Features & Device Controls */}
            <div className="w-full max-w-md space-y-4">
              <div className="p-5 rounded-3xl bg-[#141420] border border-white/10 shadow-xl">
                <div className="flex items-center gap-2.5 mb-2">
                  <div className="w-7 h-7 rounded-xl bg-[#8A2BE2]/20 flex items-center justify-center text-cyan-300">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-bold text-white">Enak Android Architecture Highlights</h3>
                </div>
                <p className="text-xs text-slate-300 leading-relaxed">
                  Enak replaces arbitrary app bans with <strong>graduated interventions</strong>.
                  Long screen time is not judged; only lost intentionality triggers action.
                </p>

                <div className="grid grid-cols-2 gap-2 mt-4 text-xs font-mono">
                  <div className="p-2.5 rounded-xl bg-[#0B0B10] border border-white/5">
                    <span className="text-slate-500 block text-[10px]">Min / Target SDK</span>
                    <span className="text-white font-semibold">26 / 35</span>
                  </div>
                  <div className="p-2.5 rounded-xl bg-[#0B0B10] border border-white/5">
                    <span className="text-slate-500 block text-[10px]">UI Toolkit</span>
                    <span className="text-cyan-300 font-semibold">Jetpack Compose M3</span>
                  </div>
                  <div className="p-2.5 rounded-xl bg-[#0B0B10] border border-white/5">
                    <span className="text-slate-500 block text-[10px]">Persistence</span>
                    <span className="text-white font-semibold">Room &amp; DataStore</span>
                  </div>
                  <div className="p-2.5 rounded-xl bg-[#0B0B10] border border-white/5">
                    <span className="text-slate-500 block text-[10px]">DI / Concurrency</span>
                    <span className="text-purple-300 font-semibold">Hilt &amp; Coroutines</span>
                  </div>
                </div>
              </div>

              {/* Quick Actions Card */}
              <div className="p-5 rounded-3xl bg-[#141420] border border-white/5 space-y-3">
                <h4 className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                  Test Interventions on Demand
                </h4>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <button
                    onClick={() => {
                      setCurrentScreen('session');
                      setActiveIntervention({
                        step: 'nudge',
                        arm: 'banner',
                        isWarning: false,
                      });
                    }}
                    className="p-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-left transition-colors"
                  >
                    <span className="font-semibold block text-cyan-300">Step 1: Nudge</span>
                    <span className="text-[10px] text-slate-500">Gentle heads-up banner</span>
                  </button>

                  <button
                    onClick={() => {
                      setCurrentScreen('session');
                      setActiveIntervention({
                        step: 'reflect',
                        arm: 'question',
                        isWarning: false,
                      });
                    }}
                    className="p-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-left transition-colors"
                  >
                    <span className="font-semibold block text-cyan-300">Step 2: Reflect</span>
                    <span className="text-[10px] text-slate-500">Goal check question</span>
                  </button>

                  <button
                    onClick={() => {
                      setCurrentScreen('session');
                      setActiveIntervention({
                        step: 'friction',
                        arm: 'pause',
                        isWarning: false,
                      });
                    }}
                    className="p-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-left transition-colors"
                  >
                    <span className="font-semibold block text-purple-300">Step 3: Friction</span>
                    <span className="text-[10px] text-slate-500">4-7-8 breathing circle</span>
                  </button>

                  <button
                    onClick={() => {
                      setCurrentScreen('session');
                      setActiveIntervention({
                        step: 'extend',
                        arm: 'question',
                        isWarning: false,
                      });
                    }}
                    className="p-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-left transition-colors"
                  >
                    <span className="font-semibold block text-purple-300">Step 5: Negotiate</span>
                    <span className="text-[10px] text-slate-500">Gemini AI time extension</span>
                  </button>
                </div>
              </div>

              {/* View Native Android Project */}
              <div className="p-5 rounded-3xl bg-gradient-to-r from-[#17142A] to-[#121024] border border-[#8A2BE2]/30 flex items-center justify-between">
                <div>
                  <h4 className="text-sm font-bold text-white">Explore Full Android Project</h4>
                  <p className="text-xs text-slate-400">
                    Inspect Kotlin source code, Gradle scripts, and tests
                  </p>
                </div>
                <button
                  onClick={() => setActiveTab('inspector')}
                  className="px-3.5 py-2 rounded-xl bg-[#8A2BE2] hover:bg-[#993bee] text-white text-xs font-semibold flex items-center gap-1.5 transition-colors shadow-md"
                >
                  <span>Open Inspector</span>
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Intervention Overlay Modal */}
      {activeIntervention && (
        <InterventionModal
          step={activeIntervention.step}
          isWarning={activeIntervention.isWarning}
          warningCount={warningCount}
          personality={settings.personality}
          purpose={intentPurpose}
          elapsedMin={Math.max(1, Math.round(elapsedSec / 60))}
          plannedMin={intentDuration}
          matchedCount={Math.round(sessionSignals.purposeMatch * 10)}
          goal={intentGoal}
          onAccept={() => setActiveIntervention(null)}
          onDismiss={() => setActiveIntervention(null)}
          onNegotiateExtension={handleNegotiateExtension}
          onFinishSessionEarly={handleFinishSession}
        />
      )}

      {/* Simulated Blocker Overlay Activity */}
      {activeBlocker && (
        <BlockerOverlay
          appName={activeBlocker.app.appName}
          packageName={activeBlocker.app.packageName}
          warningNumber={activeBlocker.app.warningCount || 1}
          isHardBlock={activeBlocker.isHardBlock}
          onCloseApp={() => {
            setActiveBlocker(null);
            setCurrentScreen('focus');
          }}
          onDismissWarning={() => setActiveBlocker(null)}
          onNegotiateExtension={handleNegotiateExtension}
        />
      )}
    </div>
  );
}
