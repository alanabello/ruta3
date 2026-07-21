(function () {
  const RULES = {
    minPerKm: 400,
    minPerHour: 6000,
    platforms: ['uber', 'didi', 'indrive']
  };
  const HISTORY_KEY = 'ruta3_offers';

  function toNumber(value, fallback) {
    const number = Number(value || fallback);
    return Number.isFinite(number) ? number : Number(fallback || 0);
  }

  function round2(value) {
    return Math.round(value * 100) / 100;
  }

  function pyFloatString(value) {
    return Number.isInteger(value) ? `${value}.0` : String(value);
  }

  function loadHistory() {
    try {
      const raw = localStorage.getItem(HISTORY_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch (error) {
      return [];
    }
  }

  function saveHistory(items) {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(items));
  }

  async function getRules() {
    return { ...RULES, platforms: [...RULES.platforms] };
  }

  async function validateOffer(payload) {
    const data = payload || {};
    const platform = String(data.platform || '').toLowerCase();
    const amount = toNumber(data.amount, 0);
    const unitMode = data.mode || 'km';
    const km = toNumber(data.km, 0);
    const minutes = toNumber(data.minutes, 0);
    const minPerKm = toNumber(data.minPerKm, RULES.minPerKm);
    const minPerHour = toNumber(data.minPerHour, RULES.minPerHour);

    if (!RULES.platforms.includes(platform)) {
      throw new Error('platform not supported');
    }
    if (amount <= 0) {
      throw new Error('amount must be greater than zero');
    }

    let value;
    let threshold;
    let meets;
    let unit;
    let details;

    if (unitMode === 'km') {
      if (km <= 0) {
        throw new Error('km must be greater than zero');
      }
      value = amount / km;
      threshold = minPerKm;
      meets = value >= threshold;
      unit = '$/km';
      details = `${amount.toFixed(0)} pesos en ${pyFloatString(km)} km`;
    } else {
      if (minutes <= 0) {
        throw new Error('minutes must be greater than zero');
      }
      value = (amount / minutes) * 60;
      threshold = minPerHour;
      meets = value >= threshold;
      unit = '$/hora';
      details = `${amount.toFixed(0)} pesos en ${pyFloatString(minutes)} min`;
    }

    const item = {
      platform,
      amount: round2(amount),
      km: unitMode === 'km' ? round2(km) : null,
      minutes: unitMode === 'hora' ? round2(minutes) : null,
      mode: unitMode,
      value: round2(value),
      threshold,
      meets,
      createdAt: new Date().toISOString()
    };

    const items = loadHistory();
    items.push(item);
    saveHistory(items);

    return {
      platform,
      amount: round2(amount),
      unit,
      value: round2(value),
      threshold,
      meets,
      result: meets ? 'cumple' : 'no-cumple',
      summary: `Oferta de ${platform.toUpperCase()}: ${details} -> ${Math.round(value).toFixed(1)} ${unit}`,
      message: meets ? 'Cumple tus requisitos configurados' : 'No cumple tus requisitos configurados'
    };
  }

  window.Ruta3OfferService = {
    getRules,
    validateOffer,
    loadHistory
  };
})();
