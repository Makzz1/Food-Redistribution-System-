import { useState, useEffect } from 'react';
import { api } from '../utils/api';
import ProfileTab from './ProfileTab';
import DisputeModal from './DisputeModal';
import ReportModal from './ReportModal';
import ReviewModal from './ReviewModal';
import BottomNav from './BottomNav';
import { usePopup } from '../context/PopupContext';

export default function ReceiverDashboard({ handleLogout, emailVerified }) {
  const [activeTab, setActiveTab] = useState('home');
  const [profileImage, setProfileImage] = useState(null);
  const { showAlert } = usePopup();

  // We should also try to load the profile image on mount so the header has it immediately
  useEffect(() => {
    api.getProfileImage().then(imgData => {
      const fullImageUrl = imgData.imageUrl.startsWith('http') ? imgData.imageUrl : `${import.meta.env.VITE_API_BASE_URL}${imgData.imageUrl}`;
      setProfileImage(fullImageUrl);
    }).catch(err => {
      // no image found
    });
  }, []);

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1 className="dashboard-title">Receiver Dashboard</h1>
      </header>

      <main className="dashboard-content">
        {!emailVerified && activeTab !== 'profile' && (
          <div style={{ background: '#ff4d4d', color: 'var(--white)', padding: '1rem', textAlign: 'center', fontWeight: 'bold', marginBottom: '1rem', borderRadius: '8px' }}>
            Please verify your email address to claim food. Check your inbox for the verification link.
          </div>
        )}
        {activeTab === 'home' && <HomeTab emailVerified={emailVerified} />}
        {activeTab === 'orders' && <OrdersTab />}
        {activeTab === 'profile' && <ProfileTab onProfileImageUpdate={setProfileImage} />}
      </main>
      <BottomNav 
        tabs={[
          { id: 'home', label: 'Home' },
          { id: 'orders', label: 'Orders' }
        ]}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        profileImage={profileImage}
        handleLogout={handleLogout}
      />
    </div>
  );
}

function HomeTab({ emailVerified }) {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [radius, setRadius] = useState(30);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [claimingId, setClaimingId] = useState(null);
  const [claimQuantity, setClaimQuantity] = useState(1);
  const { showAlert } = usePopup();

  useEffect(() => {
    fetchPosts(page, radius);
  }, [page, radius]);

  const fetchPosts = async (currentPage, currentRadius) => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getNearbyFoodPosts(currentRadius, currentPage, 9);
      setPosts(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleClaim = async (foodId) => {
    if (!emailVerified) {
      showAlert("Please verify your email before claiming food.", "error");
      return;
    }
    try {
      await api.claimFood(foodId, { quantityNeeded: claimQuantity });
      showAlert('Food claimed successfully!', 'success');
      setClaimingId(null);
      fetchPosts(page, radius); // Refresh posts after claim
    } catch (err) {
      showAlert(`Error claiming food: ${err.message}`, 'error');
    }
  };

  if (loading && posts.length === 0) return <div className="loading-state">Loading food posts...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="tab-pane">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <h2>Nearby Food</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', background: 'var(--glass-bg)', padding: '0.5rem 1rem', borderRadius: '8px', border: '2px solid var(--black)' }}>
          <label htmlFor="radius-slider" style={{ fontWeight: '800', color: 'var(--black)' }}>Search Radius: {radius} km</label>
          <input 
            id="radius-slider"
            type="range" 
            min="1" 
            max="100" 
            value={radius} 
            onChange={(e) => {
              setRadius(parseInt(e.target.value));
              setPage(0);
            }} 
            style={{ cursor: 'pointer' }}
          />
        </div>
      </div>

      {posts.length === 0 ? (
        <div className="empty-state">No food posts available within {radius} km.</div>
      ) : (
        <div className="food-grid">
          {posts.map(post => (
            <div key={post.id} className="food-card" style={{ color: 'var(--black)' }}>
              <h3 style={{ fontWeight: '900', color: 'var(--black)' }}>{post.foodName}</h3>
              <p className="food-desc" style={{ fontWeight: '700', color: 'var(--black)' }}>{post.description}</p>
              <div className="food-meta">
                <span className="badge" style={{ fontWeight: '800' }}>Available Qty: {post.quantity}</span>
                <span className="donor-name">By {post.donorName || 'Donor'}</span>
              </div>
              <div className="food-details" style={{ fontWeight: '700', color: 'var(--black)' }}>
                <p><strong>📍 Pickup:</strong> {post.pickupAddress}</p>
                <p><strong>📏 Distance:</strong> {post.distanceKm} km away</p>
                <p><strong>⏳ Expires:</strong> {new Date(post.expiryTime).toLocaleString()}</p>
              </div>
              
              {claimingId === post.id ? (
                <div className="claim-form">
                  <input 
                    type="number" 
                    min="1" 
                    max={post.quantity} 
                    value={claimQuantity} 
                    onChange={e => setClaimQuantity(parseInt(e.target.value) || 1)}
                    className="input-field"
                    placeholder="Quantity"
                  />
                  <div className="claim-actions">
                    <button className="btn-primary" onClick={() => handleClaim(post.id)}>Confirm Claim</button>
                    <button className="btn-secondary" onClick={() => setClaimingId(null)}>Cancel</button>
                  </div>
                </div>
              ) : (
                <button 
                  className="btn-primary claim-btn" 
                  onClick={() => {
                    setClaimingId(post.id);
                    setClaimQuantity(1);
                  }}
                  disabled={!emailVerified}
                  title={!emailVerified ? "Please verify your email to claim food" : ""}
                >
                  Claim Food
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button 
            className="btn-secondary" 
            disabled={page === 0} 
            onClick={() => setPage(p => p - 1)}
          >
            Previous
          </button>
          <span className="page-info">Page {page + 1} of {totalPages}</span>
          <button 
            className="btn-secondary" 
            disabled={page >= totalPages - 1} 
            onClick={() => setPage(p => p + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}

function OrdersTab() {
  const [claims, setClaims] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { showAlert, showConfirm, showPrompt } = usePopup();

  // Modal states
  const [disputeClaimId, setDisputeClaimId] = useState(null);
  const [reportClaimId, setReportClaimId] = useState(null);
  const [reviewClaimId, setReviewClaimId] = useState(null);
  const [reviewedClaimIds, setReviewedClaimIds] = useState(new Set());

  // Helper to calculate distance in km
  const calculateDistance = (lat1, lon1, lat2, lon2) => {
    if (!lat1 || !lon1 || !lat2 || !lon2) return null;
    const R = 6371; // Radius of the earth in km
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a = 
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * 
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); 
    return R * c; 
  };

  useEffect(() => {
    fetchClaims(page);
  }, [page]);

  const fetchClaims = async (currentPage) => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getMyClaims(currentPage, 10);
      let loadedClaims = data.claims?.content || data.content || [];
      loadedClaims.sort((a, b) => new Date(b.claimedAt) - new Date(a.claimedAt));
      setClaims(loadedClaims);
      setTotalPages(data.claims?.totalPages || data.totalPages || 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmReceipt = async (id) => {
    const confirmed = await showConfirm("Confirm you have received the food?");
    if (!confirmed) return;
    try {
      await api.receiverConfirm(id);
      showAlert('Receipt confirmed successfully!', 'success');
      fetchClaims(page);
    } catch (err) {
      showAlert(`Error confirming receipt: ${err.message}`, 'error');
    }
  };

  const handleCancelOrder = async (claimId) => {
    const note = await showPrompt("Reason for cancellation:");
    if (!note) return;

    try {
      await api.cancelByReceiver(claimId, note);
      showAlert('Order cancelled.', 'info');
      fetchClaims(page);
    } catch (err) {
      showAlert(`Error cancelling order: ${err.message}`, 'error');
    }
  };

  const handleDisputeClick = (claim) => {
    // 1. Time check: Must have waited 45 minutes
    const claimedAtTime = new Date(claim.claimedAt).getTime();
    const now = Date.now();
    const waitTimeMs = 45 * 60 * 1000;

    if (now - claimedAtTime < waitTimeMs) {
      showAlert("You must wait at least 45 minutes after claiming before raising a dispute.", "warning");
      return;
    }

    // 2. GPS check: Must be at pickup location (within 500 meters)
    if (!navigator.geolocation) {
      showAlert("Geolocation is not supported by your browser. Cannot verify location for dispute.", "error");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        const dist = calculateDistance(latitude, longitude, claim.pickupLatitude, claim.pickupLongitude);
        
        // If dist is null, the donor might not have provided coords (older post). Allow dispute.
        if (dist !== null && dist > 0.5) {
          showAlert(`You must be at the pickup location to dispute. You are currently ${dist.toFixed(2)}km away.`, "warning");
          return;
        }

        setDisputeClaimId(claim.id);
      },
      (error) => {
        showAlert(`Location access required to verify your presence at the pickup spot: ${error.message}`, "error");
      }
    );
  };

  if (loading && claims.length === 0) return <div className="loading-state">Loading your orders...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="tab-pane">
      <h2>My Orders</h2>
      {claims.length === 0 ? (
        <div className="empty-state">You haven't placed any orders yet.</div>
      ) : (
        <div className="claims-list">
          {claims.map(claim => (
            <div key={claim.id} className="claim-card">
              <div className="claim-header">
                <h3>Order #{claim.id}</h3>
                <span className={`status-badge status-${claim.status?.toLowerCase()}`}>{claim.status}</span>
              </div>
              <div className="claim-body">
                <p><strong>Food:</strong> {claim.foodName}</p>
                <p><strong>Quantity:</strong> {claim.quantityClaimed}</p>
                <p><strong>Claimed At:</strong> {new Date(claim.claimedAt).toLocaleString()}</p>
                
                {(claim.status === 'ACTIVE' || claim.status === 'DONOR_CONFIRMED') && (
                  <div className="claim-actions" style={{ marginTop: '1rem' }}>
                    <button 
                      className="btn-primary" 
                      onClick={() => handleConfirmReceipt(claim.id)}
                      disabled={claim.status !== 'DONOR_CONFIRMED'}
                      title={claim.status !== 'DONOR_CONFIRMED' ? "Waiting for donor confirmation" : ""}
                    >
                      Confirm Receipt
                    </button>
                    {claim.status === 'ACTIVE' && (
                      <button className="btn-secondary" onClick={() => handleCancelOrder(claim.id)} style={{ color: 'var(--black)', marginLeft: '1rem' }}>Cancel Order</button>
                    )}
                    <button className="btn-secondary" onClick={() => handleDisputeClick(claim)} style={{ color: 'var(--black)', marginLeft: '1rem' }}>Dispute</button>
                  </div>
                )}
                {(claim.status === 'COMPLETED' && !reviewedClaimIds.has(claim.id)) && (
                  <div className="claim-actions" style={{ marginTop: '1rem' }}>
                    <button className="btn-primary" onClick={() => setReviewClaimId(claim.id)}>Post Review</button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button className="btn-secondary" disabled={page === 0} onClick={() => setPage(p => p - 1)} style={{ color: 'var(--black)' }}>Previous</button>
          <span className="page-info" style={{ color: 'var(--black)' }}>Page {page + 1} of {totalPages}</span>
          <button className="btn-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} style={{ color: 'var(--black)' }}>Next</button>
        </div>
      )}

      {disputeClaimId && (
        <DisputeModal
          claimId={disputeClaimId}
          onClose={() => setDisputeClaimId(null)}
          onSuccess={() => {
            setDisputeClaimId(null);
            showAlert('Dispute submitted successfully', 'success');
            fetchClaims(page);
          }}
        />
      )}

      {reportClaimId && (
        <ReportModal
          claimId={reportClaimId}
          onClose={() => setReportClaimId(null)}
          onSuccess={() => {
            setReportClaimId(null);
            showAlert('Report submitted successfully', 'success');
          }}
        />
      )}

      {reviewClaimId && (
        <ReviewModal
          claimId={reviewClaimId}
          onClose={() => setReviewClaimId(null)}
          onSuccess={() => {
            showAlert('Review submitted successfully!', 'success');
            setReviewedClaimIds(prev => new Set(prev).add(reviewClaimId));
            setReviewClaimId(null);
          }}
        />
      )}
    </div>
  );
}
