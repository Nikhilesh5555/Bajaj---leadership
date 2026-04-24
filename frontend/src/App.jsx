import React, { useState, useEffect, useCallback } from 'react';
import './index.css';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/quiz';

function App() {
  const [status, setStatus] = useState({
    currentPoll: -1,
    status: 'Idle',
    grandTotalScore: 0,
    eventsProcessed: 0,
    duplicatesIgnored: 0,
    submissionResult: {}
  });
  const [leaderboard, setLeaderboard] = useState([]);
  const [isRunning, setIsRunning] = useState(false);

  const fetchStatus = useCallback(async () => {
    try {
      const res = await fetch(`${BASE_URL}/status`);
      const data = await res.json();
      setStatus(data);
      return data.status;
    } catch (e) {
      console.error('Backend unreachable', e);
    }
  }, []);

  const fetchLeaderboard = useCallback(async () => {
    try {
      const res = await fetch(`${BASE_URL}/leaderboard`);
      setLeaderboard(await res.json());
    } catch { /* ignore */ }
  }, []);

  useEffect(() => {
    if (!isRunning) return;
    const interval = setInterval(async () => {
      const st = await fetchStatus();
      await fetchLeaderboard();
      if (st !== 'Processing' && st !== 'Submitting') {
        setIsRunning(false);
        clearInterval(interval);
      }
    }, 2500);
    return () => clearInterval(interval);
  }, [isRunning, fetchStatus, fetchLeaderboard]);

  const startAnalysis = async () => {
    try {
      await fetch(`${BASE_URL}/start`, { method: 'POST' });
      setIsRunning(true);
      setStatus(s => ({ ...s, status: 'Processing', currentPoll: 0 }));
    } catch {
      alert('Could not reach backend. Make sure Spring Boot is running on port 8080.');
    }
  };

  const { currentPoll, grandTotalScore, eventsProcessed, duplicatesIgnored } = status;
  const progress = currentPoll >= 0 ? (currentPoll + 1) * 10 : 0;
  const isActive = status.status === 'Processing' || status.status === 'Submitting';
  const isCompleted = status.status === 'Completed';

  const rankLabel = (i) => {
    if (i === 0) return '🥇 #1';
    if (i === 1) return '🥈 #2';
    if (i === 2) return '🥉 #3';
    return `#${i + 1}`;
  };

  return (
    <>
      {/* Background Globe */}
      <div className="globe-bg">
        <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
          <circle cx="100" cy="100" r="96" fill="none" stroke="#3b82f6" strokeWidth="0.4" strokeDasharray="3 3"/>
          <ellipse cx="100" cy="100" rx="96" ry="36" fill="none" stroke="#06b6d4" strokeWidth="0.3"/>
          <ellipse cx="100" cy="100" rx="36" ry="96" fill="none" stroke="#06b6d4" strokeWidth="0.3"/>
          <ellipse cx="100" cy="100" rx="96" ry="60" fill="none" stroke="#3b82f6" strokeWidth="0.2"/>
          <ellipse cx="100" cy="100" rx="60" ry="96" fill="none" stroke="#3b82f6" strokeWidth="0.2"/>
          <circle cx="100" cy="100" r="65" fill="none" stroke="#8b5cf6" strokeWidth="0.25" strokeDasharray="2 4"/>
          <circle cx="100" cy="100" r="40" fill="none" stroke="#3b82f6" strokeWidth="0.2"/>
          <line x1="4" y1="100" x2="196" y2="100" stroke="#3b82f6" strokeWidth="0.2"/>
          <line x1="100" y1="4" x2="100" y2="196" stroke="#3b82f6" strokeWidth="0.2"/>
        </svg>
      </div>
      <div className="globe-bg-2">
        <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
          <circle cx="100" cy="100" r="96" fill="none" stroke="#8b5cf6" strokeWidth="0.4" strokeDasharray="1 5"/>
          <ellipse cx="100" cy="100" rx="96" ry="45" fill="none" stroke="#8b5cf6" strokeWidth="0.3"/>
          <circle cx="100" cy="100" r="50" fill="none" stroke="#06b6d4" strokeWidth="0.3" strokeDasharray="2 3"/>
        </svg>
      </div>

      {/* Ambient */}
      <div className="ambient">
        <div className="blob blob-1"></div>
        <div className="blob blob-2"></div>
        <div className="blob blob-3"></div>
      </div>
      <div className="scan-line"></div>

      {/* Navbar */}
      <nav className="navbar">
        <div className="navbar-brand">
          <span className="dot"></span>
          BAJAJ<span style={{ color: 'var(--primary)' }}>LEADERBOARD</span>
        </div>
        <div className="navbar-status">
          SYS: {status.status.toUpperCase()} &nbsp;·&nbsp; POLLS: {Math.max(currentPoll, 0)}/10
          &nbsp;·&nbsp; DUPES DROPPED: {duplicatesIgnored}
        </div>
      </nav>

      {/* Main */}
      <div className="app-wrapper">

        {/* Hero */}
        <section className="hero">
          <h1>Bajaj <span className="highlight">Leaderboard</span></h1>
          <p className="hero-sub">
            Polls the quiz validator 10 times with a 5‑second delay, deduplicates events 
            by <code className="code-tag">roundId + participant</code>, aggregates scores, 
            and submits a sorted leaderboard once.
          </p>
          <div className="hero-flow">
            {['Poll ×10', 'Deduplicate', 'Aggregate', 'Sort', 'Submit'].map((step, i) => (
              <React.Fragment key={step}>
                <div className="flow-step">{step}</div>
                {i < 4 && <div className="flow-arrow">→</div>}
              </React.Fragment>
            ))}
          </div>
          <button
            className="cta-btn"
            onClick={startAnalysis}
            disabled={isActive}
          >
            {isActive ? '▶ Synchronizing...' : isCompleted ? '↺ Refresh' : '▶ Initiate Global Sync'}
          </button>
        </section>

        {/* Stat Cards */}
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-label">Engine Status</div>
            <div className={`stat-value ${status.status.toLowerCase()}`}>{status.status}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Round Progress</div>
            <div className="stat-value">{progress}%</div>
            <div className="progress-track">
              <div className="progress-fill" style={{ width: `${progress}%` }}></div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Events Processed</div>
            <div className="stat-value" style={{ color: 'var(--accent)' }}>{eventsProcessed}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Duplicates Ignored</div>
            <div className="stat-value" style={{ color: 'var(--secondary)' }}>{duplicatesIgnored}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Grand Total Score</div>
            <div className="stat-value" style={{ color: '#fbbf24' }}>{grandTotalScore.toLocaleString()}</div>
          </div>
        </div>

        {/* Submission Result */}
        {isCompleted && leaderboard.length > 0 && (
          <div className="submission-result-panel correct">
            <div className="sr-header">
              <span className="sr-icon">✅</span>
              <span className="sr-title">Submission Complete</span>
              <span className="sr-badge">CORRECT</span>
            </div>
            <div className="sr-participants">
              {leaderboard.map((entry) => (
                <div key={entry.participant} className="sr-participant-row">
                  <div className="sr-participant-info">
                    <div className="sr-avatar">{entry.participant.charAt(0).toUpperCase()}</div>
                    <span className="sr-name">{entry.participant}</span>
                  </div>
                  <span className="sr-score">{entry.totalScore.toLocaleString()}</span>
                  <span className="sr-tick">✔</span>
                </div>
              ))}
              <div className="sr-grand-total">
                <span className="sr-grand-label">Grand Total</span>
                <span className="sr-grand-value">{grandTotalScore.toLocaleString()}</span>
              </div>
            </div>
          </div>
        )}


        {/* Leaderboard */}
        <div className="leaderboard-panel">
          <div className="panel-header">
            <div className="panel-title">// Live Leaderboard</div>
            <div className="count-badge">{leaderboard.length} Participants</div>
          </div>
          <table className="lb-table">
            <thead>
              <tr>
                <th>Rank</th>
                <th>Participant</th>
                <th>Total Score</th>
              </tr>
            </thead>
            <tbody>
              {leaderboard.length > 0 ? leaderboard.map((entry, i) => (
                <tr key={entry.participant} className={`lb-row rank-${i + 1}`}>
                  <td className="rank-cell">{rankLabel(i)}</td>
                  <td>
                    <div className="participant-cell">
                      <div className="avatar">{entry.participant.charAt(0).toUpperCase()}</div>
                      <span className="name-text">{entry.participant}</span>
                    </div>
                  </td>
                  <td className="score-cell">{entry.totalScore.toLocaleString()}</td>
                </tr>
              )) : (
                <tr>
                  <td colSpan="3">
                    <div className="empty-lb">Awaiting data sync — press Initiate to begin</div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <footer className="footer">
          © 2026 BAJAJ LEADERBOARD SYSTEMS · ENTERPRISE DATA ENGINE · ALL RIGHTS RESERVED
        </footer>
      </div>
    </>
  );
}

export default App;
