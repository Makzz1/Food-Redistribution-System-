import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { api } from '../utils/api';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [token, setToken] = useState('');
  
  const [form, setForm] = useState({
    newPassword: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Extract token from URL query params (e.g. ?token=123-abc)
    const params = new URLSearchParams(location.search);
    const tokenParam = params.get('token');
    if (tokenParam) {
      setToken(tokenParam);
    } else {
      setError('No reset token provided in URL.');
    }
  }, [location]);

  const handleChange = (e) => {
    setForm(p => ({ ...p, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (form.newPassword !== form.confirmPassword) {
        throw new Error('Passwords do not match');
      }
      
      await api.resetPassword(token, form.newPassword, form.confirmPassword);
      // On success, go back to login
      navigate('/?reset=true');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      
      <div className="auth-card">
        <h2>Choose New Password</h2>
        <p className="tagline">Almost there! Enter your new password below.</p>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label htmlFor="newPassword">New Password</label>
            <input 
              id="newPassword" 
              name="newPassword" 
              type="password" 
              value={form.newPassword} 
              onChange={handleChange} 
              required 
              minLength="8" 
              disabled={!token}
            />
          </div>

          <div className="input-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input 
              id="confirmPassword" 
              name="confirmPassword" 
              type="password" 
              value={form.confirmPassword} 
              onChange={handleChange} 
              required 
              minLength="8"
              disabled={!token}
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading || !token}>
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>

        <div className="divider"><span>or</span></div>
        <p className="auth-footer">
          <Link to="/">Back to Login</Link>
        </p>
      </div>
    </div>
  );
}
