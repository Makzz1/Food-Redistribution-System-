import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../utils/api';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await api.forgotPassword(email);
      setSuccess(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      
      <div className="auth-card">
        <h2>Reset Password</h2>
        <p className="tagline">We'll send you a link to reset it</p>

        {error && <div className="error-message">{error}</div>}
        {success && (
          <div className="success-message">
            Check your email! If an account exists, a reset link has been sent.
          </div>
        )}

        {!success && (
          <form onSubmit={handleSubmit}>
            <div className="input-group">
              <label htmlFor="email">Email</label>
              <input 
                id="email" 
                name="email" 
                type="email" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                required 
                placeholder="you@example.com"
              />
            </div>

            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Sending...' : 'Send Reset Link'}
            </button>
          </form>
        )}

        <div className="divider"><span>or</span></div>
        <p className="auth-footer">
          Remembered it? <Link to="/">Log in here</Link>
        </p>
      </div>
    </div>
  );
}
