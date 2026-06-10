import { useState, useEffect } from 'react';
import { api } from '../utils/api';
import { usePopup } from '../context/PopupContext';
import LocationPicker from './LocationPicker';

export default function ProfileTab({ onProfileImageUpdate }) {
  const [profile, setProfile] = useState(null);
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(true);
  const { showAlert } = usePopup();
  
  // Profile Form
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [location, setLocation] = useState('');
  const [position, setPosition] = useState(null);
  const [isSaving, setIsSaving] = useState(false);

  // Password Form
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPwd, setIsChangingPwd] = useState(false);

  // Image upload
  const [selectedFile, setSelectedFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const data = await api.getUserProfile();
      setProfile(data);
      setName(data.name || '');
      setPhone(data.phoneNumber || '');
      setLocation(data.location || '');
      if (data.latitude && data.longitude) {
        setPosition({ lat: data.latitude, lng: data.longitude });
      }
      
      try {
        const imgData = await api.getProfileImage();
        const fullImageUrl = imgData.imageUrl.startsWith('http') ? imgData.imageUrl : `${import.meta.env.VITE_API_BASE_URL}${imgData.imageUrl}`;
        setImage(fullImageUrl);
        if (onProfileImageUpdate) onProfileImageUpdate(fullImageUrl);
      } catch (err) {
        console.log("No profile image found");
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Reverse geocode when position changes
  useEffect(() => {
    if (position && position.lat && position.lng) {
      const fetchAddress = async () => {
        try {
          const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${position.lat}&lon=${position.lng}`);
          if (res.ok) {
            const data = await res.json();
            if (data && data.display_name) {
              setLocation(data.display_name);
            }
          }
        } catch (e) {
          console.log("Reverse geocoding failed", e);
        }
      };
      // Only reverse geocode if location string is empty or hasn't been manually edited since the last location change.
      // But it's easier to just fetch it
      fetchAddress();
    }
  }, [position]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      const updated = await api.updateUserProfile({ 
        name, 
        phoneNumber: phone, 
        location, 
        latitude: position?.lat || null, 
        longitude: position?.lng || null 
      });
      setProfile(updated);
      showAlert('Profile updated successfully!', 'success');
    } catch (err) {
      showAlert(`Error updating profile: ${err.message}`, 'error');
    } finally {
      setIsSaving(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      showAlert("Passwords don't match!", 'error');
      return;
    }
    setIsChangingPwd(true);
    try {
      await api.changePassword({ currentPassword, newPassword, confirmPassword });
      showAlert('Password changed successfully!', 'success');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      showAlert(`Error changing password: ${err.message}`, 'error');
    } finally {
      setIsChangingPwd(false);
    }
  };

  const handleImageUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) return;
    setIsUploading(true);
    try {
      const data = await api.uploadProfileImage(selectedFile);
      const fullImageUrl = data.imageUrl.startsWith('http') ? data.imageUrl : `${import.meta.env.VITE_API_BASE_URL}${data.imageUrl}`;
      setImage(fullImageUrl);
      if (onProfileImageUpdate) onProfileImageUpdate(fullImageUrl);
      showAlert('Profile picture updated!', 'success');
      setSelectedFile(null);
    } catch (err) {
      showAlert(`Error uploading image: ${err.message}`, 'error');
    } finally {
      setIsUploading(false);
    }
  };

  if (loading) return <div>Loading profile...</div>;

  return (
    <div className="tab-pane profile-pane">
      <h2>My Profile</h2>
      
      <div className="profile-section">
        <h3>Profile Picture</h3>
        <div className="profile-image-container">
          {image ? <img src={image} alt="Profile" className="profile-image" /> : <div className="profile-image-placeholder">No Image</div>}
          <form onSubmit={handleImageUpload} className="image-upload-form">
            <input type="file" accept="image/*" onChange={e => setSelectedFile(e.target.files[0])} />
            <button type="submit" className="btn-primary" disabled={!selectedFile || isUploading}>
              {isUploading ? 'Uploading...' : 'Upload New Picture'}
            </button>
          </form>
        </div>
      </div>

      <div className="profile-section">
        <h3>Account Details</h3>
        <form onSubmit={handleUpdateProfile} className="auth-form">
          <div className="form-group">
            <label>Name</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={profile?.email} disabled />
          </div>
          <div className="form-group">
            <label>Phone Number</label>
            <input type="text" value={phone} onChange={e => setPhone(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Location (Adjust Pin)</label>
            <LocationPicker position={position} setPosition={setPosition} />
          </div>
          <div className="form-group">
            <label>Location Address</label>
            <input type="text" value={location} onChange={e => setLocation(e.target.value)} required />
          </div>
          <button type="submit" className="btn-primary" disabled={isSaving}>
            {isSaving ? 'Saving...' : 'Update Profile'}
          </button>
        </form>
      </div>

      <div className="profile-section">
        <h3>Change Password</h3>
        <form onSubmit={handleChangePassword} className="auth-form">
          <div className="form-group">
            <label>Current Password</label>
            <input type="password" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>New Password</label>
            <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Confirm New Password</label>
            <input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn-primary" disabled={isChangingPwd}>
            {isChangingPwd ? 'Changing...' : 'Change Password'}
          </button>
        </form>
      </div>
    </div>
  );
}
