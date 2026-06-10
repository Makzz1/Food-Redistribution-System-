import { useEffect, useRef, useState } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { useNavigate, Link, useLocation } from 'react-router-dom'
import { api } from '../utils/api'
import EarthGL from './EarthGL'

gsap.registerPlugin(ScrollTrigger)

export default function EarthReveal() {
  const navigate = useNavigate()
  const location = useLocation()
  const searchParams = new URLSearchParams(location.search)
  const isRegistered = searchParams.get('registered') === 'true'
  const isReset = searchParams.get('reset') === 'true'

  const sectionRef = useRef(null)
  const naahuhRef = useRef(null)
  const loginRef = useRef(null)
  const earthWrapRef = useRef(null)

  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    const ctx = gsap.context(() => {
      // ── Set initial hidden states ──
      gsap.set(naahuhRef.current, { scale: 0.9, opacity: 0, filter: 'blur(20px)' })
      gsap.set(earthWrapRef.current, { y: 250, opacity: 0 })
      gsap.set(loginRef.current, { xPercent: -50, yPercent: -30, opacity: 0, scale: 0.95 })

      // ── Trigger: animate when section enters viewport ──
      const st = ScrollTrigger.create({
        trigger: sectionRef.current,
        start: 'top 50%',
        once: true,
        onEnter: () => {
          // NAAHUH text fades in from the mist
          gsap.to(naahuhRef.current, {
            scale: 1,
            opacity: 1,
            filter: 'blur(0px)',
            duration: 2.5,
            ease: 'power2.out',
          })

          // Earth rises from below
          gsap.to(earthWrapRef.current, {
            y: 0,
            opacity: 1,
            duration: 1.4,
            delay: 0.15,
            ease: 'power3.out',
          })

          // Login box fades in above the earth
          gsap.to(loginRef.current, {
            yPercent: -50,
            opacity: 1,
            scale: 1,
            duration: 1.0,
            delay: 0.6,
            ease: 'power3.out',
          })
        }
      })

      return () => st.kill()
    }, sectionRef)

    return () => ctx.revert()
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    gsap.fromTo('.btn-login',
      { scale: 1 },
      { scale: 0.97, duration: 0.1, yoyo: true, repeat: 1 }
    )

    try {
      const data = await api.login(form.email, form.password)
      // Save tokens
      localStorage.setItem('accessToken', data.accessToken)
      if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('userId', data.userId)

      // Navigate to dashboard
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
      setLoading(false)
    }
  }

  return (
    <section ref={sectionRef} className="earth-reveal-section">

      {/* ── Earth — rises from bottom (Behind content) ── */}
      <div ref={earthWrapRef} className="earth-reveal-wrap">
        <EarthGL />
        <div className="earth-horizon-glow" />
      </div>

      {/* ── Content Layer — text at top, login below it ── */}
      <div className="reveal-content-layer">

        {/* NAAHUH Text */}
        <div ref={naahuhRef} className="naahuh-wrap" style={{ width: '100%', maxWidth: '1600px', marginBottom: '-2rem', marginTop: '-16rem', position: 'relative', zIndex: 10, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <svg viewBox="0 0 1600 600" width="100%" height="auto" style={{ overflow: 'visible' }}>
            <path id="text-curve" d="M 50 500 Q 800 0 1550 500" fill="transparent" stroke="none" />
            <text width="1600">
              <textPath href="#text-curve" startOffset="50%" textAnchor="middle" style={{ fontSize: '380px', fontWeight: 900, fill: '#f5d76e', fontFamily: '"DM Sans", sans-serif', letterSpacing: '-0.02em' }}>
                Naahuh
              </textPath>
            </text>
          </svg>
        </div>

        <p style={{
          position: 'absolute',
          bottom: '2rem',
          left: '50%',
          transform: 'translateX(-50%)',
          fontFamily: 'var(--font-ui), sans-serif',
          fontSize: '0.85rem',
          fontWeight: 800,
          color: 'rgba(245, 215, 110, 0.7)',
          letterSpacing: '0.2em',
          textTransform: 'uppercase',
          whiteSpace: 'nowrap',
          zIndex: 11
        }}>
          — no more food waste.
        </p>

        {/* Login Card */}
        <div ref={loginRef} className="login-card-center">
          <h2>Welcome Back</h2>
          <p className="tagline">Join the movement against food waste</p>

          {isRegistered && <div className="success-message" style={{ marginBottom: '1rem', padding: '0.8rem' }}>Account created successfully! Please log in.</div>}
          {isReset && <div className="success-message" style={{ marginBottom: '1rem', padding: '0.8rem' }}>Password reset successfully! Please log in.</div>}

          {error && <div className="error-message" style={{ marginBottom: '1rem', textAlign: 'center', fontSize: '0.8rem', color: '#ff6b6b' }}>{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="input-group">
              <label htmlFor="email">Email</label>
              <input
                id="email" name="email" type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={e => setForm(p => ({ ...p, email: e.target.value }))}
                required autoComplete="email"
              />
            </div>
            <div className="input-group">
              <label htmlFor="password">Password</label>
              <input
                id="password" name="password" type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={e => setForm(p => ({ ...p, password: e.target.value }))}
                required autoComplete="current-password"
              />
            </div>
            <button type="submit" className="btn-login" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>

          <div className="divider"><span>or</span></div>
          <p className="login-footer">
            New to Naahuh - n0 more?{' '}
            <Link to="/register">Create an account</Link>
            {' · '}
            <Link to="/forgot-password">Forgot password?</Link>
          </p>
        </div>

      </div>

    </section>
  )
}
