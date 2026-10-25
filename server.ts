import express, { Request, Response } from 'express';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import { GoogleGenAI } from '@google/genai';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;

app.use(express.json());

// Initialize GoogleGenAI
const apiKey = process.env.GEMINI_API_KEY;
let aiClient: GoogleGenAI | null = null;
if (apiKey) {
  aiClient = new GoogleGenAI();
}

const LANG_MAP: Record<string, string> = {
  en: 'Reply in clear, simple English.',
  hinglish: 'Reply in friendly Hinglish (Hindi words in Latin script mixed with English).',
  hi: 'Reply in simple Hindi using Devanagari script.',
};

const PERSONA_MAP: Record<string, string> = {
  calm: 'a calm, kind coach',
  funny: 'a light-hearted, witty friend',
  nerd: 'a precise, data-loving analyst',
  strict: 'a firm but respectful mentor',
};

// API: Ask Coach
app.post('/api/coach', async (req: Request, res: Response) => {
  try {
    const { messages, summary, language = 'en', personality = 'calm' } = req.body;

    const personaDesc = PERSONA_MAP[personality] || PERSONA_MAP.calm;
    const langDesc = LANG_MAP[language] || LANG_MAP.en;
    const systemInstruction = `You are Enak's coach, ${personaDesc}. You help people keep scrolling aligned with what they meant to do. Never shame long screen time; only intentionality matters. Ground every answer in the user's data below and keep replies under 120 words. ${langDesc}\n\nUSER DATA:\n${summary || 'No prior sessions recorded yet.'}`;

    if (aiClient) {
      const contents = (messages || []).map((m: { role: string; content: string }) => ({
        role: m.role === 'assistant' ? 'model' : 'user',
        parts: [{ text: m.content }],
      }));

      const response = await aiClient.models.generateContent({
        model: 'gemini-3.8-flash',
        contents,
        config: {
          systemInstruction,
          maxOutputTokens: 250,
          temperature: 0.7,
        },
      });

      const replyText = response.text || "I'm here with you. Take a deep breath and keep your intention clear.";
      return res.json({ ok: true, text: replyText });
    } else {
      // Intelligent local fallback when no key is provided
      const lastMsg = (messages && messages.length > 0) ? messages[messages.length - 1].content : '';
      let fallbackText = "You're doing great keeping your phone usage conscious. Remember, long screen time isn't a failure if it served what you came for. Notice how you feel right now.";
      if (lastMsg.toLowerCase().includes('tired') || lastMsg.toLowerCase().includes('exhaust')) {
        fallbackText = "When fatigue kicks in, our thumbs start searching for quick dopamine loops. How about setting the phone screen-down for 5 minutes of stillness?";
      } else if (lastMsg.toLowerCase().includes('drift') || lastMsg.toLowerCase().includes('lost')) {
        fallbackText = "Drifting is completely human. The win is noticing it. Close this tab or app, stretch your shoulders, and ask: what did I really need right now?";
      }
      return res.json({ ok: true, text: fallbackText });
    }
  } catch (error: any) {
    console.error('Coach API Error:', error);
    return res.status(200).json({
      ok: true,
      text: "I'm right here with you. Take a brief breath and check in on your intention."
    });
  }
});

// API: 5-minute time negotiation
app.post('/api/negotiate', async (req: Request, res: Response) => {
  try {
    const { reason, purpose, goal, elapsedMin, plannedMin } = req.body;

    const systemInstruction = `You judge a request for 5 more minutes of scrolling. Session purpose: ${purpose || 'general'}. Goal: ${goal || "none"}. Elapsed ${elapsedMin || 10} of ${plannedMin || 10} planned minutes. Accept reasons that are specific and tied to the purpose (finishing a tutorial, replying to a friend). Gently challenge vague ones ("just one more", "bored"). Start your reply with exactly ACCEPT: or CHALLENGE: then one short warm sentence.`;

    if (aiClient) {
      const response = await aiClient.models.generateContent({
        model: 'gemini-3.8-flash',
        contents: [{ role: 'user', parts: [{ text: `My reason for 5 more minutes: "${reason}"` }] }],
        config: {
          systemInstruction,
          maxOutputTokens: 100,
          temperature: 0.4,
        },
      });

      const text = response.text || '';
      const accepted = text.trim().toUpperCase().startsWith('ACCEPT');
      const message = text.replace(/^\s*(ACCEPT|CHALLENGE):\s*/i, '').trim() ||
        (accepted ? "Take your extra 5 minutes with purpose." : "Let's pause here and protect your energy.");

      return res.json({ ok: true, accepted, message });
    } else {
      const isSpecific = (reason || '').length > 12 && !/^(just|bored|idk|nothing)/i.test((reason || '').trim());
      if (isSpecific) {
        return res.json({
          ok: true,
          accepted: true,
          message: "That's a deliberate reason. 5 extra minutes logged consciously."
        });
      } else {
        return res.json({
          ok: true,
          accepted: false,
          message: "That sounds like the scroll talking. Let's take a 30-second breather instead."
        });
      }
    }
  } catch (error: any) {
    console.error('Negotiate API Error:', error);
    return res.json({
      ok: true,
      accepted: false,
      message: "Take a conscious pause before continuing."
    });
  }
});

// API: Journal Sentiment Tagging
app.post('/api/journal', async (req: Request, res: Response) => {
  try {
    const { text } = req.body;
    if (aiClient) {
      const response = await aiClient.models.generateContent({
        model: 'gemini-3.8-flash',
        contents: [{ role: 'user', parts: [{ text }] }],
        config: {
          systemInstruction: 'Classify the sentiment of this short journal entry about a phone session. Reply with exactly one word: positive, neutral, or negative.',
          maxOutputTokens: 10,
        },
      });
      const output = (response.text || '').toLowerCase();
      let sentiment = 'neutral';
      if (output.includes('positive')) sentiment = 'positive';
      else if (output.includes('negative')) sentiment = 'negative';
      return res.json({ sentiment });
    } else {
      const lower = (text || '').toLowerCase();
      if (/good|great|proud|focus|helpful|learn|calm|relaxed/.test(lower)) {
        return res.json({ sentiment: 'positive' });
      }
      if (/bad|wasted|regret|guilt|tired|hate|stuck|spiral/.test(lower)) {
        return res.json({ sentiment: 'negative' });
      }
      return res.json({ sentiment: 'neutral' });
    }
  } catch (error) {
    return res.json({ sentiment: 'neutral' });
  }
});

// Setup Vite middleware in dev or static files in production
async function startServer() {
  const isProd = process.env.NODE_ENV === 'production';
  if (!isProd) {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(path.resolve(__dirname, 'dist')));
    app.get('*', (_req, res) => {
      res.sendFile(path.resolve(__dirname, 'dist', 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Enak server running on port ${PORT}`);
  });
}

startServer();
