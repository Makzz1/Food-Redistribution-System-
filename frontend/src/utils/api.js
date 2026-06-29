const API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/v1`;

// Helper to get auth headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export const api = {
  // Auth endpoints
  login: async (email, password) => {
    const res = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Login failed');
    return data;
  },

  register: async (userData) => {
    const res = await fetch(`${API_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData),
    });
    // Note: register doesn't return JSON payload correctly on success in some cases if it's returning a String in controller, but looking at AuthController it returns RegisterResponseDTO. Let's handle both.
    let data;
    const text = await res.text();
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
    if (!res.ok) throw new Error(data.message || data.error || 'Registration failed');
    if (!data.id && data.message) throw new Error(data.message);
    return data;
  },

  forgotPassword: async (email) => {
    const res = await fetch(`${API_URL}/auth/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to request password reset');
    return data;
  },

    resetPassword: async (token, newPassword, confirmPassword) => {
    const res = await fetch(`${API_URL}/auth/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, newPassword, confirmPassword }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to reset password');
    return data;
  },

  completeProfile: async (profileData) => {
    const res = await fetch(`${API_URL}/auth/complete-profile`, {
      method: 'POST',
      headers: getAuthHeaders(), // Need auth token
      body: JSON.stringify(profileData),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to complete profile');
    return data;
  },

  // Food endpoints
  getAvailableFood: async (page = 0, size = 10) => {
    const res = await fetch(`${API_URL}/food/available?page=${page}&size=${size}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch food posts');
    return data;
  },

  getNearbyFoodPosts: async (latitude, longitude, radiusKm = 30, page = 0, size = 10) => {
    const res = await fetch(`${API_URL}/food/available/nearby?latitude=${latitude}&longitude=${longitude}&radiusKm=${radiusKm}&page=${page}&size=${size}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch nearby food posts');
    return data;
  },

  claimFood: async (foodId, claimData) => {
    const res = await fetch(`${API_URL}/food/${foodId}/claim`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(claimData),
    });
    const text = await res.text();
    if (!res.ok) throw new Error(text || 'Failed to claim food');
    return text;
  },

  getMyClaims: async (page = 0, size = 10) => {
    const res = await fetch(`${API_URL}/claims/my-claims?page=${page}&size=${size}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch claims');
    return data;
  },

  donorConfirmClaim: async (claimId) => {
    const res = await fetch(`${API_URL}/claims/${claimId}/donor-confirm`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to confirm claim');
    return data;
  },

  markReceiverNoShow: async (claimId) => {
    const res = await fetch(`${API_URL}/claims/${claimId}/no-show`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to mark no-show');
    return data;
  },

  receiverConfirm: async (claimId) => {
    const res = await fetch(`${API_URL}/claims/${claimId}/receiver-confirm`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to confirm receipt');
    return data;
  },

  cancelByReceiver: async (claimId, note = '') => {
    const url = note ? `${API_URL}/claims/${claimId}/cancel/receiver?note=${encodeURIComponent(note)}` : `${API_URL}/claims/${claimId}/cancel/receiver`;
    const res = await fetch(url, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to cancel claim');
    return data;
  },

  disputeClaim: async (claimId, reason) => {
    const res = await fetch(`${API_URL}/claims/${claimId}/dispute`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ reason }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to raise dispute');
    return data;
  },

  submitReport: async (claimId, reportData) => {
    const res = await fetch(`${API_URL}/reports/claim/${claimId}`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(reportData), // { reason, description }
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to submit report');
    return data;
  },

  submitRating: async (claimId, data) => {
    const res = await fetch(`${API_URL}/ratings/claim/${claimId}`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });
    const resData = await res.json();
    if (!res.ok) throw new Error(resData.message || 'Failed to submit rating');
    return resData;
  },

  getMyClaims: async (page = 0, size = 10, status = null) => {
    let url = `${API_URL}/claims/my-claims?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    const res = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch claims');
    return data;
  },

  // ── Admin Endpoints ────────────────────────────────────────────────────────
  
  getAllDisputedClaims: async () => {
    const res = await fetch(`${API_URL}/claims/admin/disputed`, {
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch disputed claims');
    return data;
  },

  resolveDispute: async (claimId, resolution, adminNote = '') => {
    const res = await fetch(`${API_URL}/reports/admin/claim/${claimId}/resolve`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ resolution, adminNote }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to resolve dispute');
    return data;
  },

  getAllPosts: async () => {
    const res = await fetch(`${API_URL}/admin/posts`, {
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch all posts');
    return data;
  },

  getAdminOverview: async () => {
    const res = await fetch(`${API_URL}/admin/overview`, {
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch admin overview');
    return data;
  },

  getAllClaims: async () => {
    const res = await fetch(`${API_URL}/admin/claims`, {
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch all claims');
    return data;
  },

  getAllUsers: async () => {
    const res = await fetch(`${API_URL}/admin/users`, {
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch all users');
    return data;
  },

  getUnreviewedReports: async () => {
    const res = await fetch(`${API_URL}/reports/admin/unreviewed`, {
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch unreviewed reports');
    return data;
  },

  reviewReport: async (reportId, adminNote) => {
    const res = await fetch(`${API_URL}/reports/admin/${reportId}/review`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ adminNote }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to review report');
    return data;
  },

  banUser: async (userId) => {
    const res = await fetch(`${API_URL}/reports/admin/user/${userId}/ban`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || 'Failed to ban user');
    }
    return true;
  },

  // Donor Food endpoints
  createFoodPost: async (foodData) => {
    const res = await fetch(`${API_URL}/food`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(foodData),
    });
    const text = await res.text();
    let data;
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
    if (!res.ok) throw new Error(data.message || 'Failed to create food post');
    return data;
  },

  getMyFoodPosts: async () => {
    const res = await fetch(`${API_URL}/food/my-posts`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch your food posts');
    return data;
  },

  deleteFoodPost: async (foodId) => {
    const res = await fetch(`${API_URL}/food/${foodId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    const text = await res.text();
    if (!res.ok) throw new Error(text || 'Failed to delete food post');
    return text;
  },

  uploadFoodImages: async (foodId, images) => {
    const formData = new FormData();
    // images should be an array of File objects
    for (let i = 0; i < images.length; i++) {
      formData.append('images', images[i]);
    }

    const headers = getAuthHeaders();
    delete headers['Content-Type'];

    const res = await fetch(`${API_URL}/food/${foodId}/images`, {
      method: 'POST',
      headers,
      body: formData,
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to upload images');
    return data;
  },

  // User Profile endpoints
  getUserProfile: async () => {
    const res = await fetch(`${API_URL}/users/profile`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch profile');
    return data;
  },

  updateUserProfile: async (profileData) => {
    const res = await fetch(`${API_URL}/users/profile`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(profileData),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to update profile');
    return data;
  },

  changePassword: async (passwordData) => {
    const res = await fetch(`${API_URL}/users/change-password`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(passwordData),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to change password');
    return data;
  },

  uploadProfileImage: async (file) => {
    const formData = new FormData();
    formData.append('image', file);
    
    // Note: don't set Content-Type for FormData, browser will set it with boundary
    const headers = getAuthHeaders();
    delete headers['Content-Type'];

    const res = await fetch(`${API_URL}/users/profile-image`, {
      method: 'POST',
      headers,
      body: formData,
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to upload image');
    return data;
  },

  getProfileImage: async () => {
    const res = await fetch(`${API_URL}/users/profile-image`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch profile image');
    return data;
  },

};
