import { useEffect, useRef } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

// Three text slides — Cormorant italic style, no Earth
const SLIDES = [
  {
    id: 's1',
    label: 'The hidden crisis',
    headline: 'One third of all food\nproduced is never eaten.',
    sub: (
      <>
        Every year, <span className="stat-highlight">1.3 billion tonnes</span> of food
        is lost — enough to feed every hungry person on Earth, four times over.
      </>
    ),
  },
  {
    id: 's2',
    label: 'The environmental cost',
    headline: `Food waste is the\nworld's third largest emitter.`,
    sub: (
      <>
        Rotting food releases <span className="stat-highlight">methane</span>, a gas{' '}
        <span className="stat-highlight">25× more potent</span> than CO₂ — and
        wasted food farms land the size of China, for nothing.
      </>
    ),
  },
  {
    id: 's3',
    label: 'The human cost',
    headline: '828 million people sleep\nhungry while we throw food away.',
    sub: (
      <>
        Food discarded in wealthy countries could feed the{' '}
        <span className="stat-highlight">world's hungry three times over</span>.
        The solution exists. It just needs a bridge.
      </>
    ),
  },
]

export default function ScrollTextSection() {
  const sectionRef = useRef(null)
  const slidesRef  = useRef([])
  const dotsRef    = useRef([])

  useEffect(() => {
    const ctx = gsap.context(() => {
      const slides = slidesRef.current.filter(Boolean)
      const total  = slides.length

      // First slide visible, rest hidden
      gsap.set(slides, { opacity: 0, y: 50 })
      gsap.set(slides[0], { opacity: 1, y: 0 })
      if (dotsRef.current[0]) dotsRef.current[0].classList.add('active')

      // Per-slide triggers
      slides.forEach((slide, i) => {
        const startPct = (i / total) * 100
        const endPct   = ((i + 1) / total) * 100

        ScrollTrigger.create({
          trigger:     sectionRef.current,
          start:       `${startPct}% top`,
          end:         `${endPct}% top`,
          onEnter:     () => activateSlide(i),
          onEnterBack: () => activateSlide(i),
        })
      })

      function activateSlide(idx) {
        slides.forEach((s, si) => {
          gsap.to(s, si === idx
            ? { opacity: 1, y: 0,  duration: 0.7, ease: 'power3.out' }
            : { opacity: 0, y: si < idx ? -50 : 50, duration: 0.4, ease: 'power2.in' }
          )
        })
        dotsRef.current.forEach((d, di) => {
          if (d) d.classList.toggle('active', di === idx)
        })
      }
    }, sectionRef)

    return () => ctx.revert()
  }, [])

  return (
    <section ref={sectionRef} className="scroll-section">
      <div className="sticky-wrapper">

        {SLIDES.map((slide, i) => (
          <div
            key={slide.id}
            ref={el => (slidesRef.current[i] = el)}
            className="text-block"
          >
            <p className="label">{slide.label}</p>
            <h2 className="headline">
              {slide.headline.split('\n').map((line, li) => (
                <span key={li} style={{ display: 'block' }}>{line}</span>
              ))}
            </h2>
            <p className="sub">{slide.sub}</p>
          </div>
        ))}

        <div className="scroll-hint">
          <span>Scroll</span>
          <div className="scroll-line" />
        </div>
      </div>

      <div className="progress-dots">
        {SLIDES.map((_, i) => (
          <div key={i} ref={el => (dotsRef.current[i] = el)} className="dot" />
        ))}
      </div>
    </section>
  )
}
