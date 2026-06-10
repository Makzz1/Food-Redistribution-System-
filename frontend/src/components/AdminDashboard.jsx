import { useState, useEffect } from 'react';
import { api } from '../utils/api';
import BottomNav from './BottomNav';
import ProfileTab from './ProfileTab';
import { usePopup } from '../context/PopupContext';

export default function AdminDashboard({ handleLogout }) {
  const [activeTab, setActiveTab] = useState('overview');
  const [profileImage, setProfileImage] = useState(null);

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
        <div className="logo-text">Naahuh Admin.</div>
      </header>

      <main className="dashboard-content">
        {activeTab === 'overview' && <OverviewTab />}
        {activeTab === 'disputes' && <DisputesTab />}
        {activeTab === 'reports' && <ReportsTab />}
        {activeTab === 'posts' && <AllPostsTab />}
        {activeTab === 'claims' && <AllClaimsTab />}
        {activeTab === 'users' && <AllUsersTab />}
        {activeTab === 'profile' && <ProfileTab onProfileImageUpdate={setProfileImage} />}
      </main>

      <BottomNav 
        tabs={[
          { id: 'overview', label: 'Overview', icon: '📊' },
          { id: 'disputes', label: 'Disputes', icon: '⚖️' },
          { id: 'reports', label: 'Reports', icon: '🚨' },
          { id: 'posts', label: 'Posts', icon: '📦' },
          { id: 'claims', label: 'Claims', icon: '🤝' },
          { id: 'users', label: 'Users', icon: '👥' }
        ]}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        profileImage={profileImage}
        handleLogout={handleLogout}
      />
    </div>
  );
}

function DisputesTab() {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showAlert, showPrompt } = usePopup();

  useEffect(() => {
    fetchDisputes();
  }, []);

  const fetchDisputes = async () => {
    setLoading(true);
    try {
      const data = await api.getAllDisputedClaims();
      setClaims(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleResolve = async (claimId, action) => {
    const reason = await showPrompt(`Enter reason for ${action === 'COMPLETED' ? 'resolving' : 'cancelling'} this dispute:`);
    if (!reason) return; // cancelled prompt

    try {
      await api.resolveDispute(claimId, action, reason);
      showAlert('Dispute resolved successfully!', 'info');
      fetchDisputes();
    } catch (err) {
      showAlert(`Error resolving dispute: ${err.message}`, 'error');
    }
  };

  if (loading && claims.length === 0) return <div className="loading-state">Loading disputes...</div>;

  return (
    <div className="tab-pane">
      <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900', marginBottom: '2rem' }}>Active Disputes</h2>
      {claims.length === 0 ? (
        <div className="empty-state">No active disputes to resolve.</div>
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
                <p><strong>Donor:</strong> {claim.donorName}</p>
                <p><strong>Receiver:</strong> {claim.receiverName}</p>
                <p><strong>Disputed By:</strong> {claim.disputedBy === 'DONOR' ? claim.donorName : claim.receiverName}</p>
                <p><strong>Dispute Reason:</strong> {claim.disputeReason}</p>
                
                <div className="claim-actions" style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
                  <button className="btn-primary" onClick={() => handleResolve(claim.id, 'COMPLETED')}>Resolve as Completed</button>
                  <button className="btn-secondary" onClick={() => handleResolve(claim.id, 'CANCELLED')} style={{ color: 'var(--black)' }}>Resolve as Cancelled</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function ReportsTab() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showAlert, showConfirm } = usePopup();

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const data = await api.getUnreviewedReports();
      setReports(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleReview = async (reportId) => {
    try {
      await api.reviewReport(reportId);
      showAlert('Report marked as reviewed!', 'info');
      fetchReports();
    } catch (err) {
      showAlert(`Error reviewing report: ${err.message}`, 'error');
    }
  };

  const handleBan = async (userId) => {
    const confirmed = await showConfirm("Are you sure you want to ban this user? They will not be able to log in anymore.");
    if (!confirmed) return;
    try {
      await api.banUser(userId);
      showAlert('User has been banned.', 'info');
      fetchReports();
    } catch (err) {
      showAlert(`Error banning user: ${err.message}`, 'error');
    }
  };

  if (loading && reports.length === 0) return <div className="loading-state">Loading reports...</div>;

  return (
    <div className="tab-pane">
      <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900', marginBottom: '2rem' }}>User Reports</h2>
      {reports.length === 0 ? (
        <div className="empty-state">No unreviewed reports found.</div>
      ) : (
        <div className="claims-list">
          {reports.map(report => (
            <div key={report.id} className="claim-card">
              <div className="claim-header">
                <h3>Report #{report.id} on Claim #{report.claimId}</h3>
                <span className="status-badge status-pending">{report.status}</span>
              </div>
              <div className="claim-body">
                <p><strong>Reported By:</strong> {report.reportedByEmail} ({report.reportedByRole})</p>
                <p><strong>Reported User:</strong> {report.reportedUserEmail}</p>
                <p><strong>Reason:</strong> {report.reason}</p>
                <p><strong>Description:</strong> {report.description}</p>
                
                <div className="claim-actions" style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
                  <button className="btn-primary" onClick={() => handleReview(report.id)}>Mark Reviewed</button>
                  <button className="btn-secondary" onClick={() => handleBan(report.reportedUserId)} style={{ background: '#ff4d4d', color: 'white', border: 'none' }}>Ban User</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function AllPostsTab() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showAlert, showConfirm } = usePopup();

  useEffect(() => {
    fetchPosts();
  }, []);

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const data = await api.getAllPosts();
      setPosts(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (postId) => {
    const confirmed = await showConfirm('Are you sure you want to delete this post?');
    if (!confirmed) return;
    try {
      await api.adminDeletePost(postId);
      showAlert('Post deleted successfully', 'info');
      fetchPosts();
    } catch (err) {
      showAlert(`Error deleting post: ${err.message}`, 'error');
    }
  };

  const handleRestore = async (postId) => {
    const confirmed = await showConfirm('Are you sure you want to restore this post?');
    if (!confirmed) return;
    try {
      await api.adminRestorePost(postId);
      showAlert('Post restored successfully', 'info');
      fetchPosts();
    } catch (err) {
      showAlert(`Error restoring post: ${err.message}`, 'error');
    }
  };

  if (loading && posts.length === 0) return <div className="loading-state">Loading posts...</div>;

  return (
    <div className="tab-pane">
      <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900', marginBottom: '2rem' }}>Every Post</h2>
      {posts.length === 0 ? (
        <div className="empty-state">No posts found.</div>
      ) : (
        <div className="claims-list">
          {posts.map(post => (
            <div key={post.id} className="claim-card">
              <div className="claim-header">
                <h3>Post #{post.id}: {post.foodName}</h3>
                <span className={`status-badge status-${post.status?.toLowerCase()}`}>{post.status}</span>
              </div>
              <div className="claim-body">
                <p><strong>Donor:</strong> {post.donorName}</p>
                <p><strong>Quantity:</strong> {post.quantity}</p>
                <p><strong>Description:</strong> {post.description}</p>
                <p><strong>Expiry:</strong> {new Date(post.expiryTime).toLocaleString()}</p>
                
                <div className="claim-actions" style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
                  {post.status !== 'COMPLETED' && post.status !== 'DELETED' && (
                    <button className="btn-secondary" onClick={() => handleDelete(post.id)} style={{ background: 'rgba(255, 77, 77, 0.2)', color: '#ff4d4d', border: '1px solid rgba(255, 77, 77, 0.4)' }}>Delete Post</button>
                  )}
                  {post.status === 'DELETED' && (
                    <button className="btn-secondary" onClick={() => handleRestore(post.id)} style={{ background: 'rgba(100, 180, 255, 0.2)', color: '#80c4ff', border: '1px solid rgba(100, 180, 255, 0.4)' }}>Restore Post</button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function AllClaimsTab() {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchClaims();
  }, []);

  const fetchClaims = async () => {
    setLoading(true);
    try {
      const data = await api.getAllClaims();
      setClaims(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && claims.length === 0) return <div className="loading-state">Loading claims...</div>;

  return (
    <div className="tab-pane">
      <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900', marginBottom: '2rem' }}>Every Claim</h2>
      {claims.length === 0 ? (
        <div className="empty-state">No claims found.</div>
      ) : (
        <div className="claims-list">
          {claims.map(claim => (
            <div key={claim.id} className="claim-card">
              <div className="claim-header">
                <h3>Claim #{claim.id} for {claim.foodName}</h3>
                <span className={`status-badge status-${claim.status?.toLowerCase()}`}>{claim.status}</span>
              </div>
              <div className="claim-body">
                <p><strong>Donor:</strong> {claim.donorName}</p>
                <p><strong>Receiver:</strong> {claim.receiverName}</p>
                <p><strong>Quantity:</strong> {claim.quantityClaimed}</p>
                <p><strong>Claimed At:</strong> {new Date(claim.claimedAt).toLocaleString()}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function AllUsersTab() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const { showAlert, showConfirm } = usePopup();

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await api.getAllUsers();
      setUsers(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleBan = async (userId) => {
    const confirmed = await showConfirm("Are you sure you want to ban this user?");
    if (!confirmed) return;
    try {
      await api.banUser(userId);
      showAlert('User has been banned.', 'info');
      fetchUsers();
    } catch (err) {
      showAlert(`Error banning user: ${err.message}`, 'error');
    }
  };

  if (loading && users.length === 0) return <div className="loading-state">Loading users...</div>;

  return (
    <div className="tab-pane">
      <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900', marginBottom: '2rem' }}>Users & Trust Scores</h2>
      {users.length === 0 ? (
        <div className="empty-state">No users found.</div>
      ) : (
        <div className="claims-list">
          {users.map(user => (
            <div key={user.userId} className="claim-card">
              <div className="claim-header">
                <h3>{user.name}</h3>
                <span className={`status-badge status-active`}>{user.trustTier} ({user.trustScore}%)</span>
              </div>
              <div className="claim-body">
                <p><strong>Rating:</strong> {user.rating?.toFixed(1) || 'N/A'} ⭐</p>
                <p><strong>Successful Pickups:</strong> {user.successfulPickups || 0}</p>
                <p><strong>Successful Donations:</strong> {user.successfulDonations || 0}</p>
                <p><strong>No Shows:</strong> {user.noShowCount || 0}</p>
                <p><strong>Reports against user:</strong> {user.reportCount || 0}</p>
                <p><strong>Disputes involved:</strong> {user.disputeCount || 0}</p>
                
                <div className="claim-actions" style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
                  <button className="btn-secondary" onClick={() => handleBan(user.userId)} style={{ background: '#ff4d4d', color: 'white', border: 'none' }}>Ban User</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function OverviewTab() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const { showAlert } = usePopup();

  useEffect(() => {
    fetchOverview();
  }, []);

  const fetchOverview = async () => {
    try {
      const data = await api.getAdminOverview();
      setStats(data);
    } catch (err) {
      console.error(err);
      showAlert(`Failed to fetch overview: ${err.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  if (loading || !stats) return <div className="loading-state">Loading overview...</div>;

  return (
    <div className="tab-pane">
      <h2 style={{ fontFamily: 'var(--font-story)', fontSize: '3rem', color: 'var(--black)', fontWeight: '900', marginBottom: '2rem' }}>Platform Overview</h2>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem' }}>
        <div className="claim-card" style={{ padding: '2rem', textAlign: 'center', background: 'white' }}>
          <h3 style={{ fontSize: '1.2rem', color: '#666', marginBottom: '0.5rem' }}>Total Users</h3>
          <p style={{ fontSize: '3rem', fontWeight: '900', color: 'var(--black)' }}>{stats.totalUsers}</p>
        </div>
        <div className="claim-card" style={{ padding: '2rem', textAlign: 'center', background: 'white' }}>
          <h3 style={{ fontSize: '1.2rem', color: '#666', marginBottom: '0.5rem' }}>Total Food Posts</h3>
          <p style={{ fontSize: '3rem', fontWeight: '900', color: 'var(--black)' }}>{stats.totalPosts}</p>
        </div>
        <div className="claim-card" style={{ padding: '2rem', textAlign: 'center', background: 'white' }}>
          <h3 style={{ fontSize: '1.2rem', color: '#666', marginBottom: '0.5rem' }}>Total Claims</h3>
          <p style={{ fontSize: '3rem', fontWeight: '900', color: 'var(--black)' }}>{stats.totalClaims}</p>
        </div>
        <div className="claim-card" style={{ padding: '2rem', textAlign: 'center', background: '#e0ffe0' }}>
          <h3 style={{ fontSize: '1.2rem', color: '#666', marginBottom: '0.5rem' }}>Successful Donations</h3>
          <p style={{ fontSize: '3rem', fontWeight: '900', color: '#28a745' }}>{stats.successfulDonations}</p>
        </div>
        <div className="claim-card" style={{ padding: '2rem', textAlign: 'center', background: stats.activeDisputes > 0 ? '#ffe0e0' : 'white' }}>
          <h3 style={{ fontSize: '1.2rem', color: '#666', marginBottom: '0.5rem' }}>Active Disputes</h3>
          <p style={{ fontSize: '3rem', fontWeight: '900', color: stats.activeDisputes > 0 ? '#dc3545' : 'var(--black)' }}>{stats.activeDisputes}</p>
        </div>
      </div>
    </div>
  );
}
