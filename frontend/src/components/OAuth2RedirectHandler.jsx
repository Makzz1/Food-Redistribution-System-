import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

export default function OAuth2RedirectHandler() {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Extract the token from the URL (e.g., ?token=eyJ...)
    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get('token');

    if (token) {
      // Save it to localStorage so future API calls include it
      localStorage.setItem('accessToken', token);

      try {
        // Decode the token to inspect the custom claims
        const decoded = jwtDecode(token);
        
        // Ensure userId is saved if backend provides it in the token
        // Fallback: If not in token, you might need an API call to get it. 
        // For now, we'll just check profileComplete.
        
        if (decoded.profileComplete === false) {
          // If the profile is incomplete, redirect them to the setup screen
          navigate('/complete-profile');
        } else {
          // Profile is complete! Send them to the dashboard
          navigate('/dashboard');
        }
      } catch (err) {
        console.error("Failed to decode token", err);
        navigate('/?error=invalid_token');
      }
    } else {
      // No token found
      navigate('/?error=oauth2_failed');
    }
  }, [location, navigate]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: '#f5d76e', fontFamily: 'var(--font-ui)' }}>
      <h2>Authenticating...</h2>
    </div>
  );
}
