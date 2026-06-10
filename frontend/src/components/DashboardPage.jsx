import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ReceiverDashboard from './ReceiverDashboard';
import DonorDashboard from './DonorDashboard';
import AdminDashboard from './AdminDashboard';
import { api } from '../utils/api';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [role, setRole] = useState(null);
  const [emailVerified, setEmailVerified] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if authenticated
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/');
      return;
    }

    api.getUserProfile().then(profile => {
      setRole(profile.role);
      setEmailVerified(profile.emailVerified);
    }).catch(err => {
      console.error(err);
      // If unauthorized, navigate to home
      navigate('/');
    }).finally(() => {
      setLoading(false);
    });

  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    navigate('/');
  };

  if (loading) {
    return <div className="loading-state">Loading dashboard...</div>;
  }

  return (
    <div className="dashboard-page">

      {role === 'ADMIN' ? (
        <AdminDashboard handleLogout={handleLogout} />
      ) : role === 'DONOR' ? (
        <DonorDashboard handleLogout={handleLogout} emailVerified={emailVerified} />
      ) : (
        <ReceiverDashboard handleLogout={handleLogout} emailVerified={emailVerified} />
      )}
    </div>
  );
}
