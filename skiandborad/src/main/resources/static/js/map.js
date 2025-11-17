// 지도 초기화
const map = L.map('map').setView([36.5, 127.8], 7);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '© OpenStreetMap'
}).addTo(map);

const skiLayer = L.layerGroup().addTo(map);

async function loadResorts(type) {
  skiLayer.clearLayers();
  const url = type ? `/api/resorts?type=${type}` : `/api/resorts`;
  const res = await fetch(url);
  const data = await res.json();

  const bounds = [];
  data.forEach(r => {
    const marker = L.marker([r.lat, r.lng]).addTo(skiLayer); // ✅ 기본 마커 사용
    marker.bindPopup(`<b>${r.name}</b><br>${r.description ?? ''}`);
    bounds.push([r.lat, r.lng]);
  });

  if (bounds.length) map.fitBounds(bounds, { padding: [20, 20] });
}

loadResorts();
