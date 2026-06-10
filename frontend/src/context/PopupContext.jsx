import React, { createContext, useContext, useState, useCallback } from 'react';
import '../index.css';

const PopupContext = createContext();

export const usePopup = () => {
  return useContext(PopupContext);
};

export const PopupProvider = ({ children }) => {
  const [toast, setToast] = useState(null);
  const [confirmModal, setConfirmModal] = useState(null);
  const [promptModal, setPromptModal] = useState(null);

  const showAlert = useCallback((message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000); // auto dismiss
  }, []);

  const showConfirm = useCallback((message) => {
    return new Promise((resolve) => {
      setConfirmModal({
        message,
        onConfirm: () => {
          setConfirmModal(null);
          resolve(true);
        },
        onCancel: () => {
          setConfirmModal(null);
          resolve(false);
        }
      });
    });
  }, []);

  const showPrompt = useCallback((message, defaultValue = '') => {
    return new Promise((resolve) => {
      setPromptModal({
        message,
        value: defaultValue,
        onConfirm: (val) => {
          setPromptModal(null);
          resolve(val);
        },
        onCancel: () => {
          setPromptModal(null);
          resolve(null);
        }
      });
    });
  }, []);

  return (
    <PopupContext.Provider value={{ showAlert, showConfirm, showPrompt }}>
      {children}
      
      {/* Toast Overlay */}
      {toast && (
        <div className="toast-overlay">
          <div className={`toast-card toast-${toast.type}`}>
            {toast.message}
          </div>
        </div>
      )}

      {/* Confirm Modal Overlay */}
      {confirmModal && (
        <div className="popup-backdrop">
          <div className="glass-modal">
            <h3 className="modal-title">Confirm Action</h3>
            <p className="modal-message">{confirmModal.message}</p>
            <div className="modal-actions">
              <button className="btn-secondary" onClick={confirmModal.onCancel}>Cancel</button>
              <button className="btn-primary" onClick={confirmModal.onConfirm}>Confirm</button>
            </div>
          </div>
        </div>
      )}

      {/* Prompt Modal Overlay */}
      {promptModal && (
        <div className="popup-backdrop">
          <div className="glass-modal">
            <h3 className="modal-title">{promptModal.message}</h3>
            <input 
              type="text" 
              className="modal-input"
              defaultValue={promptModal.value}
              onChange={(e) => {
                promptModal.currentValue = e.target.value;
              }}
              autoFocus
            />
            <div className="modal-actions">
              <button className="btn-secondary" onClick={promptModal.onCancel}>Cancel</button>
              <button className="btn-primary" onClick={() => promptModal.onConfirm(promptModal.currentValue || promptModal.value)}>Submit</button>
            </div>
          </div>
        </div>
      )}
    </PopupContext.Provider>
  );
};
