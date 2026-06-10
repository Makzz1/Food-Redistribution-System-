import { useState } from 'react';
import { createPortal } from 'react-dom';
import { api } from '../utils/api';

export default function ReviewModal({ claimId, onClose, onSuccess }) {
  const [stars, setStars] = useState(5);
  const [review, setReview] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.submitRating(claimId, { stars, review });
      onSuccess();
    } catch (err) {
      setError(err.message || 'Failed to submit review');
    } finally {
      setLoading(false);
    }
  };

  return createPortal(
    <div style={modalOverlayStyle}>
      <div style={modalContentStyle}>
        <h3 style={{ color: 'var(--black)', fontWeight: '900', marginBottom: '1rem' }}>Post a Review</h3>
        {error && <div className="error-message" style={{ marginBottom: '1rem' }}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label style={{ fontWeight: '800', color: 'var(--black)' }}>Rating (1-5 Stars)</label>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
              {[1, 2, 3, 4, 5].map((star) => (
                <span
                  key={star}
                  onClick={() => setStars(star)}
                  style={{
                    fontSize: '2rem',
                    cursor: 'pointer',
                    color: star <= stars ? '#FFD700' : '#ccc',
                    transition: 'color 0.2s ease-in-out'
                  }}
                >
                  ★
                </span>
              ))}
            </div>
          </div>
          <div className="form-group" style={{ marginTop: '1rem' }}>
            <label style={{ fontWeight: '800', color: 'var(--black)' }}>Review Comments</label>
            <textarea 
              value={review}
              onChange={e => setReview(e.target.value)}
              rows={4}
              style={{ width: '100%', padding: '0.8rem', marginTop: '0.5rem', borderRadius: '4px', border: '1px solid #ccc' }}
              placeholder="Share your experience (optional)..."
            />
          </div>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
            <button type="button" onClick={onClose} className="btn-secondary" style={{ color: 'var(--black)' }}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Submitting...' : 'Post Review'}
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
