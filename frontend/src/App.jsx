import { useEffect, useRef } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import ScrollTextSection from './components/ScrollTextSection'
import EarthReveal from './components/EarthReveal'
import RegisterPage from './components/RegisterPage'
import ForgotPasswordPage from './components/ForgotPasswordPage'
import ResetPasswordPage from './components/ResetPasswordPage'
import DashboardPage from './components/DashboardPage'
import { PopupProvider } from './context/PopupContext'

gsap.registerPlugin(ScrollTrigger)

function LandingPage() {
  const overlayRef = useRef(null)

  useEffect(() => {
    // Smooth white → transparent fade-in on load
    const overlay = overlayRef.current
    if (!overlay) return;
    gsap.set(overlay, { opacity: 1 })
    gsap.to(overlay, {
      opacity: 0, duration: 2.0, ease: 'power2.inOut', delay: 0.1,
      onComplete: () => { overlay.style.display = 'none' }
    })
  }, [])

  return (
    <>
      {/* White fade-in overlay */}
      <div ref={overlayRef} id="page-overlay" aria-hidden="true" />

      {/* Fixed vivid sky photo */}


      {/* Scrollable content */}
      <div className="content-layer">
        {/* 1. Text-only scroll story (3 slides) */}
        <ScrollTextSection />

        {/* 2. NAAHUH from top + Earth from bottom + Login box in middle (triggers after all text) */}
        <EarthReveal />
      </div>
    </>
  )
}

import CelestialInkShader from './components/CelestialInkShader'

export default function App() {
  return (
    <PopupProvider>
      <CelestialInkShader />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
        </Routes>
      </BrowserRouter>
    </PopupProvider>
  )
}
