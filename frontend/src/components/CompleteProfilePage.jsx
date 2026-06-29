import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { gsap } from 'gsap';
import { api } from '../utils/api';

export default function CompleteProfilePage() {
  const navigate = useNavigate();
  const formRef = useRef(null);

  const [form, setForm] = useState({
    role: '',
    location: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Fade in animation matching the rest of the site
    gsap.fromTo(formRef.current, 
      { opacity: 0, y: 30 }, 
      { opacity: 1, y: 0, duration: 1, ease: 'power3.out' }
    );
  }, []);

  const handleGeolocation = () => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;
          try {
            const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
            const data = await res.json();
            const locationStr = data.display_name || `${lat}, ${lon}`;
            setForm(prev => ({ ...prev, location: locationStr, latitude: lat, longitude: lon }));
          } catch (err) {
            console.error("Geocoding failed", err);
            setForm(prev => ({ ...prev, location: `${lat}, ${lon}`, latitude: lat, longitude: lon }));
          }
        },
        (err) => {
          setError('Could not get location. Please allow location access or type it manually (this version requires auto-location for exact coords).');
        }
      );
    } else {
      setError('Geolocation is not supported by your browser.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    if (!form.role) {
      setError("Please select a role.");
      setLoading(false);
      return;
    }

    if (!form.latitude || !form.longitude || !form.location) {
      setError("Please detect your location using the button provided.");
      setLoading(false);
      return;
    }

    try {
      // POST the missing details to complete the profile
      const data = await api.completeProfile({
        role: form.role,
        location: form.location,
        latitude: form.latitude,
        longitude: form.longitude
      });
      
      // Update tokens in localStorage with the fully completed profile tokens
      localStorage.setItem('accessToken', data.accessToken);
      if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('userId', data.userId);

      // Redirect to dashboard
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#111',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem'
    }}>
      <div ref={formRef} className="login-card-center" style={{ maxWidth: '450px', width: '100%', position: 'relative' }}>
        <h2>Almost Done!</h2>
        <p className="tagline">We just need a couple more details.</p>

        {error && <div className="error-message" style={{ marginBottom: '1rem', textAlign: 'center', fontSize: '0.8rem', color: '#ff6b6b' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          
          <div className="input-group">
            <label>I want to...</label>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
              <label style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="role" 
                  value="DONOR" 
                  checked={form.role === 'DONOR'}
                  onChange={e => setForm(p => ({ ...p, role: e.target.value }))}
                />
                Donate Food
              </label>
              <label style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="role" 
                  value="RECEIVER" 
                  checked={form.role === 'RECEIVER'}
                  onChange={e => setForm(p => ({ ...p, role: e.target.value }))}
                />
                Receive Food
              </label>
            </div>
          </div>

          <div className="input-group" style={{ marginTop: '1.5rem' }}>
            <label htmlFor="location">Your Location</label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                id="location"
                name="location"
                type="text"
                placeholder="Detect location..."
                value={form.location}
                readOnly
                style={{ flex: 1, cursor: 'not-allowed' }}
              />
              <button 
                type="button" 
                className="btn-secondary" 
                onClick={handleGeolocation}
                style={{ padding: '0 1rem', whiteSpace: 'nowrap' }}
              >
                Detect
              </button>
            </div>
            <small style={{ color: '#888', marginTop: '0.5rem', display: 'block', fontSize: '0.75rem' }}>
              We need exact coordinates to match donors and receivers nearby.
            </small>
          </div>

          <button type="submit" className="btn-login" disabled={loading} style={{ marginTop: '2rem' }}>
            {loading ? 'Saving...' : 'Complete Profile'}
          </button>
        </form>
      </div>
    </div>
  );
}
