import { useState } from 'react';
import { createPortal } from 'react-dom';
import { api } from '../utils/api';

export default function ReportModal({ claimId, onClose, onSuccess }) {
  const [reason, setReason] = useState('OTHER');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const REASONS = [
    'FAKE_POSTING',
    'FOOD_NOT_AVAILABLE',
    'FOOD_NOT_RECEIVED',
    'WRONG_LOCATION',
    'DONOR_UNREACHABLE',
    'RECEIVER_NO_SHOW',
    'ABUSIVE_BEHAVIOR',
    'OTHER'
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.submitReport(claimId, { reason, description });
      onSuccess();
    } catch (err) {
      setError(err.message || 'Failed to submit report');
    } finally {
      setLoading(false);
    }
  };

  return createPortal(
    <div style={modalOverlayStyle}>
      <div style={modalContentStyle}>
        <h3 style={{ color: 'var(--black)', fontWeight: '900', marginBottom: '1rem' }}>Submit Report</h3>
        {error && <div className="error-message" style={{ marginBottom: '1rem' }}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label style={{ fontWeight: '800', color: 'var(--black)' }}>Reason</label>
            <select 
              value={reason} 
              onChange={e => setReason(e.target.value)}
              style={{ width: '100%', padding: '0.8rem', marginTop: '0.5rem', borderRadius: '4px', border: '1px solid #ccc' }}
            >
              {REASONS.map(r => (
                <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>
          <div className="form-group" style={{ marginTop: '1rem' }}>
            <label style={{ fontWeight: '800', color: 'var(--black)' }}>Description</label>
            <textarea 
              value={description}
              onChange={e => setDescription(e.target.value)}
              required
              rows={4}
              style={{ width: '100%', padding: '0.8rem', marginTop: '0.5rem', borderRadius: '4px', border: '1px solid #ccc' }}
              placeholder="Provide details about the issue..."
            />
          </div>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
            <button type="button" onClick={onClose} className="btn-secondary" style={{ color: 'var(--black)' }}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Report'}
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
