import { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { usePopup } from '../context/PopupContext';
import L from 'leaflet';

// Fix for default marker icon in leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

function LocationMarker({ position, setPosition }) {
  useMapEvents({
    click(e) {
      setPosition(e.latlng);
    },
  });

  return position === null ? null : (
    <Marker position={position}></Marker>
  );
}

function MapController({ position }) {
  const map = useMap();
  useEffect(() => {
    if (position) {
      map.flyTo(position, map.getZoom());
    }
  }, [position, map]);
  return null;
}

export default function LocationPicker({ position, setPosition }) {
  const defaultCenter = [51.505, -0.09]; 
  const [isLocating, setIsLocating] = useState(false);
  const { showAlert } = usePopup();

  const locateUser = () => {
    if (!navigator.geolocation) {
      showAlert("Geolocation is not supported by your browser", "error");
      return;
    }
    setIsLocating(true);
    navigator.geolocation.getCurrentPosition(
      (loc) => {
        setPosition({ lat: loc.coords.latitude, lng: loc.coords.longitude });
        setIsLocating(false);
      },
      (err) => {
        showAlert("Could not fetch location. Please enable location services or click on the map manually.", "warning");
        setIsLocating(false);
      }
    );
  };

  useEffect(() => {
    // Try to get user's current location on load only if no position exists
    if (!position) {
      locateUser();
    }
  }, []); // Run only once on mount

  return (
    <div style={{ marginBottom: '1rem' }}>
      <button 
        type="button" 
        onClick={locateUser} 
        className="btn-secondary" 
        style={{ marginBottom: '10px', fontSize: '0.9rem' }}
        disabled={isLocating}
      >
        {isLocating ? 'Locating...' : '📍 Use Current GPS Location'}
      </button>
      <div className="map-container" style={{ height: '300px', width: '100%', border: '2px solid var(--black)' }}>
        <MapContainer 
          center={position || defaultCenter} 
          zoom={13} 
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <LocationMarker position={position} setPosition={setPosition} />
          <MapController position={position} />
        </MapContainer>
      </div>
      <p style={{ fontSize: '0.85rem', marginTop: '5px', color: 'var(--black)' }}>
        * You can click anywhere on the map to adjust the pickup location manually.
      </p>
    </div>
  );
}
