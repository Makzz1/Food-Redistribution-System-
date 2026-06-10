import React from 'react';
import '../index.css';

const AnimatedNavLink = ({ href, children }) => {
  return (
    <a href={href} className="bottom-nav-link">
      <div className="bottom-nav-link-inner">
        <span className="bottom-nav-text-default">{children}</span>
        <span className="bottom-nav-text-hover">{children}</span>
      </div>
    </a>
  );
};

export default function BottomNav({ tabs, activeTab, setActiveTab, profileImage, handleLogout }) {
  // If no tabs are passed, render nothing (or you could render a default view if needed)
  if (!tabs) return null;

  return (
    <header className="bottom-navbar">
      <nav style={{ display: 'flex', gap: '2rem', alignItems: 'center' }}>
        {tabs.map((tab) => (
          <button 
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className="bottom-nav-link"
            style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
          >
            <div className="bottom-nav-link-inner">
              <span className="bottom-nav-text-default" style={{ color: activeTab === tab.id ? 'var(--yellow)' : 'rgba(255, 255, 255, 0.6)' }}>{tab.label}</span>
              <span className="bottom-nav-text-hover">{tab.label}</span>
            </div>
          </button>
        ))}

        <div style={{ width: '1px', height: '1.5rem', background: 'rgba(255,255,255,0.2)', margin: '0 0.5rem' }}></div>

        {/* Profile */}
        <button 
          onClick={() => setActiveTab('profile')}
          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, display: 'flex', alignItems: 'center' }}
        >
          {profileImage ? (
            <img src={profileImage} alt="Profile" style={{ width: '32px', height: '32px', borderRadius: '50%', objectFit: 'cover', border: activeTab === 'profile' ? '2px solid var(--yellow)' : '2px solid transparent' }} />
          ) : (
             <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--black)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--white)', fontWeight: 'bold', fontSize: '0.9rem', border: activeTab === 'profile' ? '2px solid var(--yellow)' : '2px solid transparent' }}>
                U
              </div>
          )}
        </button>

        {/* Logout */}
        <button 
          onClick={handleLogout}
          className="bottom-nav-link"
          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, marginLeft: '0.5rem' }}
        >
          <div className="bottom-nav-link-inner">
            <span className="bottom-nav-text-default">LOGOUT</span>
            <span className="bottom-nav-text-hover" style={{ color: '#ff4d4d' }}>LOGOUT</span>
          </div>
        </button>
      </nav>
    </header>
  );
}
