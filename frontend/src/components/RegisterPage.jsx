import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../utils/api';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'RECEIVER',
    phoneNumber: '',
    location: '',
    latitude: null,
    longitude: null
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [locating, setLocating] = useState(false);

  const handleChange = (e) => {
    setForm(p => ({ ...p, [e.target.name]: e.target.value }));
  };

  const handleGetLocation = () => {
    setLocating(true);
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser');
      setLocating(false);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        
        let locName = `${lat.toFixed(4)}, ${lon.toFixed(4)}`;
        
        try {
          // Reverse geocoding to get a human-readable location name
          const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
          if (response.ok) {
            const data = await response.json();
            if (data && data.display_name) {
              // Usually the first few parts are most relevant (e.g. City, State)
              // But we can just use the full display name or a shortened version
              locName = data.display_name;
            }
          }
        } catch (e) {
          console.error("Failed to fetch location name:", e);
        }
        
        setForm(p => ({
          ...p,
          latitude: lat,
          longitude: lon,
          location: locName
        }));
        setLocating(false);
        setError(null);
      },
      (err) => {
        setError('Unable to retrieve your location');
        setLocating(false);
      }
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (form.latitude === null || form.longitude === null) {
        throw new Error('Please fetch your location first');
      }
      if (form.phoneNumber.length !== 10) {
        throw new Error('Phone number must be exactly 10 digits');
      }
      
      await api.register(form);
      // On success, go back to login
      navigate('/?registered=true');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      
      <div className="auth-card">
        <h2>Join Naahuh - n0 more</h2>
        <p className="tagline">Create an account to help end food waste</p>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label htmlFor="name">{form.role === 'RECEIVER' ? 'Organization Name' : 'Full Name'}</label>
            <input id="name" name="name" type="text" value={form.name} onChange={handleChange} required />
          </div>

          <div className="input-group">
            <label htmlFor="email">Email</label>
            <input id="email" name="email" type="email" value={form.email} onChange={handleChange} required />
          </div>

          <div className="input-group">
            <label htmlFor="password">Password</label>
            <input id="password" name="password" type="password" value={form.password} onChange={handleChange} required minLength="8" />
          </div>

          <div className="input-row">
            <div className="input-group">
              <label htmlFor="role">I want to...</label>
              <select id="role" name="role" value={form.role} onChange={handleChange} className="auth-select">
                <option value="RECEIVER">Receive Food</option>
                <option value="DONOR">Donate Food</option>
              </select>
            </div>

            <div className="input-group">
              <label htmlFor="phoneNumber">Phone (10 digits)</label>
              <input id="phoneNumber" name="phoneNumber" type="text" value={form.phoneNumber} onChange={handleChange} required pattern="\d{10}" title="10 digit phone number" />
            </div>
          </div>

          <div className="input-group">
            <label htmlFor="location">Location</label>
            <div className="location-row">
              <input id="location" name="location" type="text" value={form.location} readOnly required placeholder="Click button to auto-fill →" />
              <button type="button" className="btn-secondary" onClick={handleGetLocation} disabled={locating}>
                {locating ? '...' : '📍 Find Me'}
              </button>
            </div>
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
          
          <div className="divider" style={{ margin: '1.5rem 0' }}><span>or</span></div>
            
          <button 
            type="button" 
            className="btn-primary" 
            style={{ backgroundColor: '#ffffff', color: '#757575', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', fontWeight: 600, border: '1px solid #ddd' }}
            onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/google'}
          >
            <svg width="18" height="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
              <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.7 17.74 9.5 24 9.5z"/>
              <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.9c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.13-10.36 7.13-17.65z"/>
              <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
              <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
              <path fill="none" d="M0 0h48v48H0z"/>
            </svg>
            Sign up with Google
          </button>
        </form>

        <p className="auth-footer" style={{ marginTop: '1.5rem' }}>
          Already have an account? <Link to="/">Log in</Link>
        </p>
      </div>
    </div>
  );
}
