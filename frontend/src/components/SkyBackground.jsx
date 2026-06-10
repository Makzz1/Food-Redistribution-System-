import { useEffect, useRef } from 'react'
import { gsap } from 'gsap'

/* ── individual cloud SVG blob ── */
function CloudBlob({ width, opacity, blur }) {
  return (
    <svg
      width={width}
      viewBox="0 0 200 80"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      style={{ filter: `blur(${blur}px)`, opacity }}
    >
      <ellipse cx="100" cy="55" rx="90" ry="28" fill="white" />
      <ellipse cx="70"  cy="45" rx="55" ry="35" fill="white" />
      <ellipse cx="130" cy="48" rx="48" ry="30" fill="white" />
      <ellipse cx="100" cy="38" rx="40" ry="30" fill="white" />
    </svg>
  )
}

const CLOUDS = [
  { id: 'c1', top: '8%',  size: 260, opacity: 0.85, blur: 0,   speed: 80,  startX: -300 },
  { id: 'c2', top: '14%', size: 180, opacity: 0.65, blur: 1,   speed: 110, startX: '110vw' },
  { id: 'c3', top: '22%', size: 320, opacity: 0.90, blur: 0,   speed: 95,  startX: -400 },
  { id: 'c4', top: '30%', size: 200, opacity: 0.55, blur: 2,   speed: 130, startX: '115vw' },
  { id: 'c5', top: '38%', size: 140, opacity: 0.40, blur: 3,   speed: 150, startX: -200 },
  { id: 'c6', top: '6%',  size: 210, opacity: 0.70, blur: 1,   speed: 100, startX: '105vw' },
  { id: 'c7', top: '18%', size: 160, opacity: 0.50, blur: 2,   speed: 120, startX: -250 },
  { id: 'c8', top: '42%', size: 280, opacity: 0.75, blur: 1,   speed: 88,  startX: '108vw' },
]

export default function SkyBackground() {
  const containerRef = useRef(null)

  useEffect(() => {
    if (!containerRef.current) return
    const cloudEls = containerRef.current.querySelectorAll('.cloud')
    const tweens = []

    cloudEls.forEach((el, i) => {
      const cfg = CLOUDS[i]
      const goRight = cfg.startX === -300 || cfg.startX === -400 || cfg.startX === -200 || cfg.startX === -250
      const fromX = goRight ? cfg.startX : '110vw'
      const toX   = goRight ? '115vw' : -350

      // Initial position
      gsap.set(el, { x: fromX })

      // Floating drift loop
      const floatTween = gsap.to(el, {
        x: toX,
        duration: cfg.speed,
        ease: 'none',
        repeat: -1,
        onRepeat: () => {
          gsap.set(el, { x: fromX })
        }
      })

      // Gentle vertical bob
      const bobTween = gsap.to(el, {
        y: '+=18',
        duration: 6 + i * 1.2,
        ease: 'sine.inOut',
        yoyo: true,
        repeat: -1,
      })

      tweens.push(floatTween, bobTween)
    })

    return () => tweens.forEach(t => t.kill())
  }, [])

  return (
    <div className="sky-canvas">
      <div className="sky-gradient" />
      <div className="stars-layer" />
      <div className="sun-glow" />

      {/* animated clouds */}
      <div ref={containerRef} style={{ position: 'absolute', inset: 0, overflow: 'hidden' }}>
        {CLOUDS.map((cfg, i) => (
          <div
            key={cfg.id}
            className="cloud"
            style={{ top: cfg.top, left: 0 }}
          >
            <CloudBlob width={cfg.size} opacity={cfg.opacity} blur={cfg.blur} />
          </div>
        ))}
      </div>
    </div>
  )
}
