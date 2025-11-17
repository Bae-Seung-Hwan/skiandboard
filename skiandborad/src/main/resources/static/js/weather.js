(function () {
  function getCtx() {
    const m = document.querySelector('meta[name="ctx"]');
    return (m && m.content) ? m.content : '/';
  }

  async function fetchJson(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status} for ${url}`);
    return res.json();
  }

  // resort.html/detail.html에서 호출
  window.loadWeather = async function(resortId) {
    const ctx = getCtx(); // 예: "/skiandboard/"
    const el = document.getElementById('weather');
    if (!el) return;

    try {
      const [w, c] = await Promise.all([
        fetchJson(`${ctx}api/weather/${resortId}`),
        fetchJson(`${ctx}api/congestion/${resortId}`)
      ]);

      el.innerHTML = `
        <div><b>현재 기온</b>: ${w.temperatureC.toFixed(1)}°C</div>
        <div><b>신설 적설</b>: ${w.snowfallCm.toFixed(1)} cm</div>
        <div><b>풍속</b>: ${w.windMs.toFixed(1)} m/s, <b>상태</b>: ${w.condition}</div>
        <div class="crowd chip level${c.level}">
          혼잡도: <b>${c.level}/5</b> (${c.label}) <small style="opacity:.7">${c.reason}</small>
        </div>
        <small>${new Date(w.fetchedAt).toLocaleString()}</small>
      `;
    } catch (e) {
      console.error(e);
      el.textContent = '날씨/혼잡 정보를 불러오지 못했습니다.';
    }
  };
})();
