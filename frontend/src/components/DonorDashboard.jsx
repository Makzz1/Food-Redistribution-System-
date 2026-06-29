import { useState, useEffect } from 'react';
import { api } from '../utils/api';
import ProfileTab from './ProfileTab';
import LocationPicker from './LocationPicker';
import DisputeModal from './DisputeModal';
import ReportModal from './ReportModal';
import ReviewModal from './ReviewModal';
import BottomNav from './BottomNav';
import { usePopup } from '../context/PopupContext';

export default function DonorDashboard({ handleLogout, emailVerified }) {
  const [activeTab, setActiveTab] = useState('home');
  const [profileImage, setProfileImage] = useState(null);
  const { showAlert } = usePopup();

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
        <div className="logo-text">Naahuh - n0 more.</div>
      </header>

      <main className="dashboard-content">
        {!emailVerified && activeTab !== 'profile' && (
          <div style={{ background: '#ff4d4d', color: 'var(--white)', padding: '1rem', textAlign: 'center', fontWeight: 'bold', marginBottom: '1rem', borderRadius: '8px' }}>
            Please verify your email address to create food posts. Check your inbox for the verification link.
          </div>
        )}
        {activeTab === 'home' && <HomeTab emailVerified={emailVerified} />}
        {activeTab === 'claims' && <ClaimsTab />}
        {activeTab === 'profile' && <ProfileTab onProfileImageUpdate={setProfileImage} />}
      </main>
      <BottomNav 
        tabs={[
          { id: 'home', label: 'Home' },
          { id: 'claims', label: 'Claims' }
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
  const [loading, setLoading] = useState(true);
  const { showAlert, showConfirm } = usePopup();
  
  // Create form state
  const [isCreating, setIsCreating] = useState(false);
  const [foodName, setFoodName] = useState('');
  const [description, setDescription] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [position, setPosition] = useState(null);
  const [expiryHours, setExpiryHours] = useState(24);
  const [selectedImages, setSelectedImages] = useState([]);
  
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    fetchMyPosts();
  }, []);

  const fetchMyPosts = async () => {
    setLoading(true);
    try {
      const data = await api.getMyFoodPosts();
      setPosts(data.filter(p => p.status !== 'DELETED'));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleImageSelect = (e) => {
    setSelectedImages([...e.target.files]);
  };

  const handleCreatePost = async (e) => {
    e.preventDefault();
    if (!emailVerified) {
      showAlert("Please verify your email before posting food.", "error");
      return;
    }
    if (!position) {
      showAlert("Please select a pickup location on the map.", "error");
      return;
    }
    
    setIsSubmitting(true);
    try {
      let finalAddress = 'Pinned Location';
      try {
        const nomRes = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${position.lat}&lon=${position.lng}`);
        if (nomRes.ok) {
          const nomData = await nomRes.json();
          if (nomData && nomData.display_name) {
            finalAddress = nomData.display_name;
          }
        }
      } catch (err) {
        console.warn("Could not reverse geocode address", err);
      }

      const expiryTime = new Date(Date.now() + expiryHours * 60 * 60 * 1000).toISOString();
      const foodData = {
        foodName,
        description,
        quantity,
        pickupAddress: finalAddress,
        latitude: position.lat,
        longitude: position.lng,
        expiryTime
      };

      const newPost = await api.createFoodPost(foodData);
      
      if (selectedImages.length > 0 && newPost.id) {
        try {
          const formData = new FormData();
          selectedImages.forEach(file => {
            formData.append('files', file);
          });
          await api.uploadFoodImages(newPost.id, formData);
        } catch (imgErr) {
          console.error("Image upload failed:", imgErr);
          showAlert("Post created, but some images failed to upload.", "warning");
        }
      }
      
      showAlert('Food post created successfully!', 'success');
      
      setIsCreating(false);
      setFoodName('');
      setDescription('');
      setQuantity(1);
      setPosition(null);
      setSelectedImages([]);
      
      fetchMyPosts();
    } catch (err) {
      showAlert(`Error creating post: ${err.message}`, 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeletePost = async (id) => {
    const confirmed = await showConfirm('Are you sure you want to delete this post?');
    if (!confirmed) return;
    try {
      await api.deleteFoodPost(id);
      fetchMyPosts();
    } catch (err) {
      showAlert(`Error deleting post: ${err.message}`, 'error');
    }
  };

  return (
    <div className="tab-pane">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900' }}>My Food Posts</h2>
        <button 
          className="btn-primary" 
          onClick={() => setIsCreating(!isCreating)}
          disabled={!emailVerified}
          title={!emailVerified ? "Please verify your email to post food" : ""}
        >
          {isCreating ? 'Cancel Creation' : '+ New Post'}
        </button>
      </div>

      {isCreating && (
        <div className="create-post-form" style={{ background: 'var(--glass-bg)', padding: '2rem', border: '3px solid var(--black)', borderRadius: '12px', marginBottom: '2rem' }}>
          <h3 style={{ color: 'var(--black)', fontWeight: '900', marginBottom: '1rem' }}>Create New Food Post</h3>
          <form onSubmit={handleCreatePost} className="auth-form" style={{ maxWidth: '100%' }}>
            <div className="form-group">
              <label style={{ fontWeight: '800', color: 'var(--black)' }}>Food Name</label>
              <input type="text" value={foodName} onChange={e => setFoodName(e.target.value)} required />
            </div>
            <div className="form-group">
              <label style={{ fontWeight: '800', color: 'var(--black)' }}>Description</label>
              <textarea value={description} onChange={e => setDescription(e.target.value)} required rows={3}></textarea>
            </div>
            <div className="form-group">
              <label style={{ fontWeight: '800', color: 'var(--black)' }}>Quantity</label>
              <input type="number" min="1" value={quantity} onChange={e => setQuantity(parseInt(e.target.value))} required />
            </div>
            <div className="form-group">
              <label style={{ fontWeight: '800', color: 'var(--black)' }}>Pin Location on Map</label>
              <LocationPicker position={position} setPosition={setPosition} />
            </div>
            <div className="form-group">
              <label style={{ fontWeight: '800', color: 'var(--black)' }}>Expires In (Hours)</label>
              <input type="number" min="1" value={expiryHours} onChange={e => setExpiryHours(parseInt(e.target.value))} required />
            </div>
            <div className="form-group">
              <label style={{ fontWeight: '800', color: 'var(--black)' }}>Upload Images</label>
              <input type="file" multiple accept="image/*" onChange={handleImageSelect} />
              {selectedImages.length > 0 && <small>{selectedImages.length} file(s) selected</small>}
            </div>
            <button type="submit" className="btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Creating...' : 'Create Post'}
            </button>
          </form>
        </div>
      )}

      {loading ? (
        <div>Loading posts...</div>
      ) : posts.length === 0 ? (
        <div className="empty-state">You haven't posted any food yet.</div>
      ) : (
        <div className="food-grid">
          {posts.map(post => (
            <div key={post.id} className="food-card" style={{ color: 'var(--black)' }}>
              <h3 style={{ fontWeight: '900', color: 'var(--black)' }}>{post.foodName}</h3>
              <p className="food-desc" style={{ fontWeight: '700', color: 'var(--black)' }}>{post.description}</p>
              <div className="food-meta">
                <span className="badge" style={{ fontWeight: '800' }}>Qty: {post.remainingQuantity}</span>
                <span className={`status-badge status-${post.status?.toLowerCase()}`} style={{ fontWeight: '800' }}>{post.status}</span>
              </div>
              <div className="food-details" style={{ fontWeight: '700', color: 'var(--black)' }}>
                <p><strong>📍 Pickup:</strong> {post.pickupAddress}</p>
                <p><strong>⏳ Expires:</strong> {new Date(post.expiryTime).toLocaleString()}</p>
              </div>
              <div className="claim-actions" style={{ marginTop: '1rem' }}>
                <button className="btn-secondary" onClick={() => handleDeletePost(post.id)}>Delete Post</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function ClaimsTab() {
  const [claims, setClaims] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const { showAlert, showConfirm } = usePopup();

  // Modal states
  const [reportClaimId, setReportClaimId] = useState(null);
  const [reviewClaimId, setReviewClaimId] = useState(null);
  const [reviewedClaimIds, setReviewedClaimIds] = useState(new Set());

  useEffect(() => {
    fetchClaims(page);
  }, [page]);

  const fetchClaims = async (currentPage) => {
    setLoading(true);
    try {
      const data = await api.getMyClaims(currentPage, 10);
      let loadedClaims = data.claims?.content || data.content || [];
      // Order by created_at (claimedAt) descending (newest first)
      loadedClaims.sort((a, b) => new Date(b.claimedAt) - new Date(a.claimedAt));
      setClaims(loadedClaims);
      setTotalPages(data.claims?.totalPages || data.totalPages || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (id) => {
    const confirmed = await showConfirm("Confirm you have handed over the food to the receiver?");
    if (!confirmed) return;
    try {
      // It might be named donorConfirmClaim in your old api.js, let's use the one we added or check
      await (api.donorConfirm ? api.donorConfirm(id) : api.donorConfirmClaim(id));
      showAlert('Claim confirmed successfully!', 'success');
      fetchClaims(page);
    } catch (err) {
      showAlert(`Error confirming claim: ${err.message}`, 'error');
    }
  };

  const handleNoShow = async (id) => {
    const confirmed = await showConfirm("Mark receiver as no-show? This will cancel their claim.");
    if (!confirmed) return;
    try {
      await api.markReceiverNoShow(id);
      showAlert('Receiver marked as no-show.', 'success');
      fetchClaims(page);
    } catch (err) {
      showAlert(`Error: ${err.message}`, 'error');
    }
  };

  if (loading && claims.length === 0) return <div className="loading-state">Loading claims...</div>;

  return (
    <div className="tab-pane">
      <h2>Claims on My Posts</h2>
      {claims.length === 0 ? (
        <div className="empty-state">No claims on your posts yet.</div>
      ) : (
        <div className="claims-list">
          {claims.map(claim => (
            <div key={claim.id} className="claim-card">
              <div className="claim-header">
                <h3>Order #{claim.id} - {claim.foodName}</h3>
                <span className={`status-badge status-${claim.status?.toLowerCase()}`}>{claim.status}</span>
              </div>
              <div className="claim-body">
                <p><strong>Receiver:</strong> {claim.receiverName}</p>
                <p><strong>Quantity Claimed:</strong> {claim.quantityClaimed}</p>
                <p><strong>Claimed At:</strong> {new Date(claim.claimedAt).toLocaleString()}</p>
                
                {claim.status === 'ACTIVE' && (
                  <div className="claim-actions" style={{ marginTop: '1rem' }}>
                    <button className="btn-primary" onClick={() => handleConfirm(claim.id)}>Confirm Handoff</button>
                    <button className="btn-secondary" onClick={() => handleNoShow(claim.id)} style={{ color: 'var(--black)' }}>Mark No-Show</button>
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
    </div>
  );
}
