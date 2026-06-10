import { useRef, useEffect } from 'react'
import * as THREE from 'three'

const EARTH_DAY = 'https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg'
// Other custom textures I generated for you:
// const EARTH_DAY = '/img/cartoon.png'
// const EARTH_DAY = '/img/minecraft.png'
// const EARTH_DAY = '/img/light.png'

const EARTH_BUMP = 'https://unpkg.com/three-globe/example/img/earth-topology.png'
const EARTH_SPEC = 'https://upload.wikimedia.org/wikipedia/commons/1/16/Earthspec1k.jpg'
const CLOUDS_IMG = 'https://unpkg.com/three-globe/example/img/earth-water.png'

export default function EarthGL() {
  const mountRef = useRef(null)

  useEffect(() => {
    const el = mountRef.current
    if (!el) return

    const w = el.clientWidth
    const h = el.clientHeight

    // ── Scene + Camera ──
    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(38, w / h, 0.1, 100)
    // Pull back enough to show the full circle in the square canvas
    camera.position.set(0, 0, 3.2)

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    renderer.setSize(w, h)
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.15
    el.appendChild(renderer.domElement)

    // ── Lighting — match reference (sun from upper-right) ──
    scene.add(new THREE.AmbientLight(0xffffff, 0.12))

    const sun = new THREE.DirectionalLight(0xfff5e0, 2.8)
    sun.position.set(2.5, 1.2, 2)
    scene.add(sun)

    const fillLight = new THREE.DirectionalLight(0x4488cc, 0.2)
    fillLight.position.set(-2, 0, 1)
    scene.add(fillLight)

    // ── Earth sphere ──
    const loader = new THREE.TextureLoader()
    const geo = new THREE.SphereGeometry(1, 72, 72)

    const earthMat = new THREE.MeshPhongMaterial({
      map: loader.load(EARTH_DAY),
      bumpMap: loader.load(EARTH_BUMP),
      bumpScale: 0.06,
      specularMap: loader.load(EARTH_SPEC),
      specular: new THREE.Color(0x336688),
      shininess: 10,
    })
    const earthMesh = new THREE.Mesh(geo, earthMat)
    // Africa/Middle East facing the camera — matching reference image
    // rotation.y ≈ 0.2 shows Africa center, like the third reference image
    earthMesh.rotation.y = 0.2
    scene.add(earthMesh)

    // ── Thin atmosphere (very subtle) ──
    const atmosMesh = new THREE.Mesh(
      new THREE.SphereGeometry(1.018, 48, 48),
      new THREE.MeshPhongMaterial({ color: 0x4499ff, transparent: true, opacity: 0.05, side: THREE.FrontSide })
    )
    atmosMesh.rotation.y = 0.2
    scene.add(atmosMesh)

    // ── Cloud layer ──
    const cloudMesh = new THREE.Mesh(
      new THREE.SphereGeometry(1.010, 48, 48),
      new THREE.MeshPhongMaterial({ map: loader.load(CLOUDS_IMG), transparent: true, opacity: 0.42, depthWrite: false })
    )
    cloudMesh.rotation.y = 0.2
    scene.add(cloudMesh)

    // ── Slow auto-rotation (always running) ──
    let animId
    const animate = () => {
      animId = requestAnimationFrame(animate)
      earthMesh.rotation.y += 0.0001
      cloudMesh.rotation.y += 0.00015
      atmosMesh.rotation.y += 0.0001
      renderer.render(scene, camera)
    }
    animate()

    const onResize = () => {
      const nw = el.clientWidth, nh = el.clientHeight
      camera.aspect = nw / nh
      camera.updateProjectionMatrix()
      renderer.setSize(nw, nh)
    }
    window.addEventListener('resize', onResize)

    return () => {
      cancelAnimationFrame(animId)
      window.removeEventListener('resize', onResize)
      renderer.dispose()
      if (el.contains(renderer.domElement)) el.removeChild(renderer.domElement)
    }
  }, [])

  return <div ref={mountRef} style={{ width: '100%', height: '100%' }} />
}
