(function () {
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  function getCtx() {
    const m = document.querySelector('meta[name="ctx"]');
    return (m && m.content) ? m.content : '/';
  }

  async function fetchJson(url) {
    const r = await fetch(url);
    if (!r.ok) throw new Error(`HTTP ${r.status} for ${url}`);
    return r.json();
  }

  function saveState(s) { localStorage.setItem('rec.filters', JSON.stringify(s)); }
  function loadState() {
    try { return JSON.parse(localStorage.getItem('rec.filters')) || null; }
    catch { return null; }
  }

  async function getLocation() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject(new Error('geolocation not supported'));
      navigator.geolocation.getCurrentPosition(
        pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        err => reject(err),
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
      );
    });
  }

  function bindTabs(root) {
    const tabs = $$('.tab', root);
    const panels = $$('.tab-panel', root);
    tabs.forEach(btn => {
      btn.addEventListener('click', () => {
        tabs.forEach(b => b.classList.remove('active'));
        panels.forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        const idx = btn.dataset.tab === 'list' ? '#rec-list' : '#rec-filters';
        $(idx, root).classList.add('active');
      });
    });
  }

  function renderList(root, items) {
    const wrap = $('#rec-items', root);
    if (!wrap) return;
    if (!items || items.length === 0) {
      wrap.innerHTML = `<div class="empty">조건에 맞는 추천이 없습니다.</div>`;
      return;
    }
    wrap.innerHTML = items.map(x => `
      <a class="rec-item" href="${getCtx()}resorts/${x.resortId}">
        <div class="title">${x.resortName}</div>
        <div class="meta">
          <span>점수 ${x.score.toFixed(2)}</span>
          <span>거리 ${x.distanceKm.toFixed(1)}km</span>
          <span>혼잡 ${x.congestionLevel}/5</span>
        </div>
        <div class="sub">${x.summary}</div>
      </a>
    `).join('');
  }

  async function runFetch(root, state) {
    const ctx = getCtx();
    const params = new URLSearchParams({
      lat: state.lat, lng: state.lng,
      skill: state.skill,
      gear: state.gear,
      region: state.region,
      transport: state.transport
    });
    if (state.maxDistanceKm) params.set('maxDistanceKm', state.maxDistanceKm);

    $('#rec-items', root).innerHTML = `<div class="loading">불러오는 중...</div>`;
    try {
      const list = await fetchJson(`${ctx}api/recommend?${params.toString()}`);
      renderList(root, list);
      saveState(state);
      // 탭 자동 이동: 리스트로
      $$('.tab', root).forEach(b => b.classList.remove('active'));
      $('.tab[data-tab="list"]', root).classList.add('active');
      $$('.tab-panel', root).forEach(p => p.classList.remove('active'));
      $('#rec-list', root).classList.add('active');
    } catch (e) {
      console.error(e);
      $('#rec-items', root).innerHTML = `<div class="error">추천을 불러오지 못했습니다.</div>`;
    }
  }

  function init(root) {
    if (!root) return;
    bindTabs(root);

    // 상태 복원
    const s = loadState() || {
      lat: 37.5665, lng: 126.9780, // 서울시청 기본값
      skill: 'INTERMEDIATE',
      gear: 'BOTH',
      region: 'ANY',
      transport: 'CAR',
      maxDistanceKm: ''
    };

    // 필터 값 바인딩
    $('#rec-skill', root).value = s.skill;
    $('#rec-gear', root).value = s.gear;
    $('#rec-region', root).value = s.region;
    $('#rec-transport', root).value = s.transport;
    $('#rec-maxdist', root).value = s.maxDistanceKm;

    // 내 위치
    $('#rec-use-geo', root).addEventListener('click', async () => {
      const hint = $('#rec-geo-status', root);
      hint.textContent = '위치 가져오는 중...';
      try {
        const pos = await getLocation();
        s.lat = pos.lat; s.lng = pos.lng;
        hint.textContent = `위치 설정됨 (${pos.lat.toFixed(4)}, ${pos.lng.toFixed(4)})`;
      } catch (e) {
        hint.textContent = '위치 권한을 허용해주세요.';
      }
    });

    // 적용하기
    $('#rec-apply', root).addEventListener('click', () => {
      const state = {
        lat: s.lat, lng: s.lng,
        skill: $('#rec-skill', root).value,
        gear: $('#rec-gear', root).value,
        region: $('#rec-region', root).value,
        transport: $('#rec-transport', root).value,
        maxDistanceKm: $('#rec-maxdist', root).value
      };
      runFetch(root, state);
    });

    // 최초 자동 호출
    runFetch(root, s);
  }

  // 위젯 자동 초기화 (페이지에 여러 개 있어도 됨)
  document.addEventListener('DOMContentLoaded', () => {
    $$('.recommend-widget').forEach(init);
  });
})();
