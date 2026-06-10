import { useState } from 'react';
import { createPortal } from 'react-dom';
import { api } from '../utils/api';

export default function DisputeModal({ claimId, onClose, onSuccess }) {
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const REASONS = [
    'FAKE_POSTING',
    'FOOD_NOT_RECEIVED',
    'WRONG_LOCATION',
    'DONOR_UNREACHABLE',
    'RECEIVER_NO_RESPONSE',
    'ABUSIVE_BEHAVIOR',
    'OTHER'
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.disputeClaim(claimId, reason);
      onSuccess();
    } catch (err) {
      setError(err.message || 'Failed to submit dispute');
    } finally {
      setLoading(false);
    }
  };

  return createPortal(
    <div style={modalOverlayStyle}>
      <div style={modalContentStyle}>
        <h3 style={{ color: 'var(--black)', fontWeight: '900', marginBottom: '1rem' }}>Dispute Claim</h3>
        {error && <div className="error-message" style={{ marginBottom: '1rem' }}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label style={{ fontWeight: '800', color: 'var(--black)' }}>Reason for Dispute</label>
            <select 
              value={reason} 
              onChange={e => setReason(e.target.value)}
              style={{ width: '100%', padding: '0.8rem', marginTop: '0.5rem', borderRadius: '4px', border: '1px solid #ccc' }}
            >
              <option value="" disabled>Select a reason...</option>
              {REASONS.map(r => (
                <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>
          <p style={{ fontSize: '0.8rem', color: 'var(--black)', marginBottom: '1rem' }}>
            * Note: Depending on your role, you must wait up to 45 minutes before raising a dispute for an active order.
          </p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button type="button" onClick={onClose} className="btn-secondary" style={{ color: 'var(--black)' }}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading || !reason}>
              {loading ? 'Submitting...' : 'Submit Dispute'}
            </button>
          </div>
        </form>
      </div>
    </div>,
    document.body
  );
}

const modalOverlayStyle = {
  position: 'fixed',
  top: 0, left: 0, right: 0, bottom: 0,
  backgroundColor: 'rgba(0,0,0,0.5)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  zIndex: 1000
};

const modalContentStyle = {
  backgroundColor: '#fff',
  padding: '2rem',
  borderRadius: '12px',
  width: '90%',
  maxWidth: '400px',
  color: 'var(--black)'
};
