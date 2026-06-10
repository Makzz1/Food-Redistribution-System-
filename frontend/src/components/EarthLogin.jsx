import { useEffect, useRef, useState } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

export default function EarthLogin() {
  const sectionRef = useRef(null)
  const cardRef    = useRef(null)
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.fromTo(cardRef.current,
        { y: 50, opacity: 0 },
        {
          y: 0, opacity: 1,
          duration: 0.9,
          ease: 'power3.out',
          scrollTrigger: {
            trigger: sectionRef.current,
            start: 'top 80%',
            toggleActions: 'play none none reverse',
          }
        }
      )
    }, sectionRef)
    return () => ctx.revert()
  }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    setLoading(true)
    gsap.fromTo('.btn-login',
      { scale: 1 },
      { scale: 0.97, duration: 0.1, yoyo: true, repeat: 1,
        onComplete: () => setTimeout(() => setLoading(false), 1200) }
    )
    console.log('Login:', form)
    // TODO: POST /api/auth/login
  }

  return (
    <section ref={sectionRef} className="login-only-section">
      <div ref={cardRef} className="login-card">
        <h2>Welcome Back</h2>
        <p className="tagline">Join the movement against food waste</p>

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
          <a href="/register">Create an account</a>
          {' · '}
          <a href="/forgot-password">Forgot password?</a>
        </p>
      </div>
    </section>
  )
}
