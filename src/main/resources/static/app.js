'use strict'

// ══════════════════════════════════════════════════════════════════════════════
//  CONSTANTS
// ══════════════════════════════════════════════════════════════════════════════

const STATUT_LABELS = {
  CONTACTE:        'Contacté',
  EN_DISCUSSION:   'En discussion',
  ENTRETIEN_PREVU: 'Entretien prévu',
  ENTRETIEN_PASSE: 'Entretien passé',
  DEVIS_ENVOYE:    'Devis envoyé',
  OFFRE_RECUE:     'Offre reçue',
  GAGNE:           'Gagné',
  PERDU:           'Perdu',
}

const STATUT_COLORS = {
  CONTACTE:        'bg-slate-100 text-slate-700',
  EN_DISCUSSION:   'bg-blue-100 text-blue-700',
  ENTRETIEN_PREVU: 'bg-indigo-100 text-indigo-700',
  ENTRETIEN_PASSE: 'bg-violet-100 text-violet-700',
  DEVIS_ENVOYE:    'bg-amber-100 text-amber-800',
  OFFRE_RECUE:     'bg-orange-100 text-orange-700',
  GAGNE:           'bg-green-100 text-green-700',
  PERDU:           'bg-red-100 text-red-700',
}

const TYPE_LABELS = {
  MISSION_FREELANCE: 'Mission freelance',
  CDI:               'CDI',
  SITE_WEB:          'Site web',
}

const CANAL_LABELS = {
  LINKEDIN:       'LinkedIn',
  EMAIL:          'Email',
  TELEPHONE:      'Téléphone',
  RECOMMANDATION: 'Recommandation',
  JOBBOARD:       'Job board',
}

const RELANCE_STATUT_LABELS = {
  EN_ATTENTE:   'En attente',
  ENVOYEE:      'Envoyée',
  SANS_REPONSE: 'Sans réponse',
}

const RELANCE_STATUT_COLORS = {
  EN_ATTENTE:   'bg-yellow-100 text-yellow-700',
  ENVOYEE:      'bg-blue-100 text-blue-700',
  SANS_REPONSE: 'bg-red-100 text-red-700',
}

// ══════════════════════════════════════════════════════════════════════════════
//  STATE
// ══════════════════════════════════════════════════════════════════════════════

const S = {
  token:             localStorage.getItem('crm_token'),
  username:          localStorage.getItem('crm_username'),
  inscriptionOuverte: true,
  view:     'dashboard',
  contacts: { items: [], page: 0, totalPages: 0, totalElements: 0 },
  opps:     { items: [], page: 0, totalPages: 0, totalElements: 0, statut: '', type: '' },
  aRelancer: [],
  stats:    null,
}

// ══════════════════════════════════════════════════════════════════════════════
//  API
// ══════════════════════════════════════════════════════════════════════════════

async function apiCall(method, path, body) {
  const headers = { 'Content-Type': 'application/json' }
  if (S.token) headers['Authorization'] = `Bearer ${S.token}`
  const res = await fetch(path, {
    method,
    headers,
    body: body != null ? JSON.stringify(body) : undefined,
  })
  if (res.status === 401) { doLogout(); return null }
  if (res.status === 204) return null
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || `Erreur ${res.status}`)
  return data
}

const GET  = p     => apiCall('GET',    p)
const POST = (p,b) => apiCall('POST',   p, b)
const PUT  = (p,b) => apiCall('PUT',    p, b)
const DEL  = p     => apiCall('DELETE', p)

// ══════════════════════════════════════════════════════════════════════════════
//  AUTH
// ══════════════════════════════════════════════════════════════════════════════

function setAuth(token, username) {
  S.token = token; S.username = username
  localStorage.setItem('crm_token', token)
  localStorage.setItem('crm_username', username)
}

function doLogout() {
  S.token = null; S.username = null
  localStorage.removeItem('crm_token')
  localStorage.removeItem('crm_username')
  render()
}

// ══════════════════════════════════════════════════════════════════════════════
//  UTILS
// ══════════════════════════════════════════════════════════════════════════════

function esc(s) {
  if (s == null) return ''
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')
}

function fmtDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('fr-FR')
}

function fmtDateTime(dt) {
  if (!dt) return '—'
  const d = new Date(dt)
  return d.toLocaleDateString('fr-FR') + ' ' + d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

function fmtMoney(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(v)
}

function montantLabel(o) {
  if (o.type === 'MISSION_FREELANCE') return o.tjm    ? fmtMoney(o.tjm) + '/j'   : '—'
  if (o.type === 'CDI')              return o.salaire ? fmtMoney(o.salaire)        : '—'
  if (o.type === 'SITE_WEB')         return o.budget  ? fmtMoney(o.budget)         : '—'
  return '—'
}

function badge(cls, label) {
  return `<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${cls}">${esc(label)}</span>`
}

function statutBadge(s) {
  return badge(STATUT_COLORS[s] || 'bg-gray-100 text-gray-600', STATUT_LABELS[s] || s)
}

function relanceBadge(s) {
  return badge(RELANCE_STATUT_COLORS[s] || 'bg-gray-100 text-gray-600', RELANCE_STATUT_LABELS[s] || s)
}

// ══════════════════════════════════════════════════════════════════════════════
//  TOAST + MODAL + PAGINATION
// ══════════════════════════════════════════════════════════════════════════════

function toast(msg, type = 'ok') {
  const el = document.getElementById('toast')
  if (!el) return
  el.textContent = msg
  el.className = `fixed bottom-6 right-6 z-50 px-4 py-3 rounded-xl shadow-xl text-sm font-medium ${
    type === 'err' ? 'bg-red-600 text-white' : 'bg-green-600 text-white'
  }`
  el.classList.remove('hidden')
  clearTimeout(el._t)
  el._t = setTimeout(() => el.classList.add('hidden'), 3500)
}

function openModal(html) {
  document.getElementById('modal-content').innerHTML = html
  document.getElementById('modal-overlay').classList.remove('hidden')
}

function closeModal() {
  document.getElementById('modal-overlay').classList.add('hidden')
}

function pagBar(page, totalPages, navFn) {
  if (totalPages <= 1) return ''
  const btn = (label, p, disabled) =>
    `<button ${disabled ? 'disabled' : `onclick="${navFn}(${p})"`}
      class="px-3 py-1.5 text-sm border border-gray-300 bg-white ${disabled ? 'text-gray-300 cursor-not-allowed' : 'hover:bg-gray-50 cursor-pointer'}">${label}</button>`
  return `<div class="flex items-center mt-5">
    <div class="inline-flex rounded-lg overflow-hidden shadow-sm">
      ${btn('‹ Préc.', page - 1, page === 0)}
      <span class="px-4 py-1.5 text-sm border-t border-b border-gray-300 bg-white text-gray-600 select-none">
        ${page + 1} / ${totalPages}
      </span>
      ${btn('Suiv. ›', page + 1, page >= totalPages - 1)}
    </div>
  </div>`
}

// ══════════════════════════════════════════════════════════════════════════════
//  LAYOUT
// ══════════════════════════════════════════════════════════════════════════════

function renderLayout(content) {
  const nav = [
    { id: 'dashboard',    label: 'Dashboard',
      icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>' },
    { id: 'contacts',     label: 'Contacts',
      icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>' },
    { id: 'opportunites', label: 'Opportunités',
      icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>' },
    { id: 'aRelancer',    label: 'À relancer',
      icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>',
      badge: S.aRelancer.length },
  ]

  return `<div class="flex h-screen overflow-hidden">
    <!-- Sidebar -->
    <aside class="w-56 bg-slate-900 flex flex-col flex-shrink-0">
      <div class="px-5 py-5 border-b border-slate-700/60">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 bg-indigo-500 rounded-xl flex items-center justify-center flex-shrink-0">
            <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
          </div>
          <span class="text-white font-semibold text-sm leading-tight">Job<br>Tracker</span>
        </div>
      </div>
      <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        ${nav.map(item => {
          const active = S.view === item.id
          return `<button onclick="navigate('${item.id}')"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              active ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white'
            }">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">${item.icon}</svg>
            <span class="flex-1 text-left">${item.label}</span>
            ${item.badge ? `<span class="bg-orange-500 text-white text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full flex-shrink-0">${item.badge}</span>` : ''}
          </button>`
        }).join('')}
      </nav>
      <div class="px-3 py-4 border-t border-slate-700/60">
        <div class="flex items-center gap-3 px-3 py-2">
          <div class="w-8 h-8 bg-indigo-700 rounded-lg flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
            ${esc((S.username || '?')[0].toUpperCase())}
          </div>
          <span class="text-slate-300 text-sm flex-1 truncate">${esc(S.username || '')}</span>
          <button onclick="doLogout()" title="Déconnexion" class="text-slate-500 hover:text-white transition-colors">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
          </button>
        </div>
      </div>
    </aside>
    <!-- Main -->
    <main class="flex-1 overflow-y-auto">${content}</main>
  </div>`
}

function pageHeader(title, sub, actions = '') {
  return `<div class="bg-white border-b border-gray-200 px-8 py-5 sticky top-0 z-10">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold text-gray-900">${title}</h1>
        ${sub ? `<p class="text-sm text-gray-500 mt-0.5">${sub}</p>` : ''}
      </div>
      ${actions ? `<div class="flex items-center gap-3">${actions}</div>` : ''}
    </div>
  </div>`
}

function primaryBtn(label, onclick) {
  return `<button onclick="${onclick}"
    class="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-4 py-2 rounded-xl transition-colors">
    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
    </svg>${label}</button>`
}

// ══════════════════════════════════════════════════════════════════════════════
//  AUTH PAGE
// ══════════════════════════════════════════════════════════════════════════════

function renderAuthPage() {
  return `<div class="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-slate-100 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-sm p-8">
      <div class="text-center mb-8">
        <div class="w-14 h-14 bg-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-indigo-200">
          <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">JobTracker</h1>
        <p class="text-gray-500 text-sm mt-1">Gérez votre pipeline commercial</p>
      </div>
      <div class="flex border-b border-gray-200 mb-6">
        <button id="tab-login" onclick="switchTab('login')"
          class="flex-1 pb-3 text-sm font-medium border-b-2 border-indigo-600 text-indigo-600">Connexion</button>
        ${S.inscriptionOuverte ? `<button id="tab-register" onclick="switchTab('register')"
          class="flex-1 pb-3 text-sm font-medium border-b-2 border-transparent text-gray-500 hover:text-gray-700">Inscription</button>` : ''}
      </div>
      <form id="form-login" onsubmit="doLogin(event)">
        <div class="space-y-4">
          ${authField('username', 'Identifiant', 'text', '')}
          ${authField('password', 'Mot de passe', 'password', '')}
          <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-medium transition-colors shadow-sm">
            Se connecter
          </button>
        </div>
      </form>
      <form id="form-register" class="hidden" onsubmit="doRegister(event)">
        <div class="space-y-4">
          ${authField('username', 'Identifiant', 'text', '')}
          ${authField('password', 'Mot de passe (min. 8 car.)', 'password', '', 'minlength="8"')}
          <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-medium transition-colors shadow-sm">
            Créer mon compte
          </button>
        </div>
      </form>
    </div>
  </div>`
}

function authField(name, label, type, value, extra = '') {
  return `<div>
    <label class="block text-sm font-medium text-gray-700 mb-1">${label}</label>
    <input name="${name}" type="${type}" required ${extra} value="${esc(value)}"
      class="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"/>
  </div>`
}

function switchTab(tab) {
  const isLogin = tab === 'login'
  document.getElementById('form-login').classList.toggle('hidden', !isLogin)
  document.getElementById('form-register').classList.toggle('hidden', isLogin)
  document.getElementById('tab-login').className    = `flex-1 pb-3 text-sm font-medium border-b-2 ${isLogin ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`
  document.getElementById('tab-register').className = `flex-1 pb-3 text-sm font-medium border-b-2 ${!isLogin ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`
}

async function doLogin(e) {
  e.preventDefault()
  const fd = new FormData(e.target)
  try {
    const res = await POST('/auth/login', { username: fd.get('username'), password: fd.get('password') })
    if (!res) return
    setAuth(res.token, res.username)
    await navigate('dashboard')
  } catch (err) { toast(err.message, 'err') }
}

async function doRegister(e) {
  e.preventDefault()
  const fd = new FormData(e.target)
  try {
    const res = await POST('/auth/register', { username: fd.get('username'), password: fd.get('password') })
    if (!res) return
    setAuth(res.token, res.username)
    await navigate('dashboard')
  } catch (err) { toast(err.message, 'err') }
}

// ══════════════════════════════════════════════════════════════════════════════
//  DASHBOARD
// ══════════════════════════════════════════════════════════════════════════════

async function loadDashboard() {
  const [stats, aRelancer] = await Promise.all([
    GET('/stats').catch(() => null),
    GET('/opportunites/a-relancer').catch(() => []),
  ])
  S.stats     = stats
  S.aRelancer = aRelancer || []
}

function renderDashboard() {
  const { stats, aRelancer } = S

  const statCard = (label, value, note, noteColor) => `
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
      <p class="text-xs font-semibold uppercase tracking-wide text-gray-400">${label}</p>
      <p class="text-3xl font-bold text-gray-900 mt-2 mb-1">${value}</p>
      ${note ? `<p class="text-xs font-medium text-${noteColor}-600">${note}</p>` : '<p class="text-xs text-transparent">-</p>'}
    </div>`

  const pipeline = stats ? fmtMoney(stats.pipelineTotal) : '—'
  const tjmMoyen = stats && stats.tjmMoyenMissionsEnCours > 0 ? fmtMoney(stats.tjmMoyenMissionsEnCours) + '/j' : '—'
  const relAtt   = stats ? stats.nbRelancesEnAttente : '—'
  const nb       = aRelancer.length
  const statuts  = stats?.nbOpportunitesParStatut || {}
  const totalOpps = Object.values(statuts).reduce((a, b) => a + Number(b), 0)

  return `${pageHeader('Dashboard', 'Vue d\'ensemble de votre activité freelance')}
  <div class="p-8">
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      ${statCard('Pipeline total',     pipeline,   totalOpps + ' opportunité(s)', 'indigo')}
      ${statCard('À relancer',         nb,         nb > 0 ? 'Action requise' : 'À jour ✓', nb > 0 ? 'orange' : 'green')}
      ${statCard('TJM moyen missions', tjmMoyen,   'Missions actives', 'blue')}
      ${statCard('Relances en attente',relAtt,     '', 'gray')}
    </div>

    ${totalOpps > 0 ? `
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 mb-6">
      <p class="text-sm font-semibold text-gray-700 mb-4">Pipeline par statut</p>
      <div class="flex flex-wrap gap-3">
        ${Object.entries(STATUT_LABELS).map(([k]) => {
          const c = Number(statuts[k] || 0)
          if (!c) return ''
          return `<div class="flex items-center gap-2">${statutBadge(k)}<span class="text-sm font-bold text-gray-800">${c}</span></div>`
        }).join('')}
      </div>
    </div>` : ''}

    ${nb > 0 ? `
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm">
      <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
        <p class="text-sm font-semibold text-gray-700">⚡ À relancer maintenant</p>
        <button onclick="navigate('aRelancer')" class="text-sm text-indigo-600 hover:text-indigo-800 font-medium">
          Voir tout →
        </button>
      </div>
      <div class="divide-y divide-gray-50">
        ${aRelancer.slice(0, 5).map(o => `
          <div class="px-5 py-3.5 flex items-center gap-4">
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">${esc(o.titre)}</p>
              <p class="text-xs text-gray-500">${esc(o.contactNom || '—')} · dernière action ${fmtDate(o.dateDerniereAction)}</p>
            </div>
            ${statutBadge(o.statut)}
            <button onclick="openRelancesModal(${o.id})"
              class="flex-shrink-0 text-xs bg-indigo-50 hover:bg-indigo-100 text-indigo-600 font-semibold px-3 py-1.5 rounded-lg transition-colors">
              Relancer
            </button>
          </div>`).join('')}
      </div>
    </div>` : `
    <div class="bg-green-50 border border-green-200 rounded-2xl p-8 text-center">
      <p class="text-3xl mb-2">✓</p>
      <p class="font-semibold text-green-700">Aucune opportunité à relancer</p>
      <p class="text-green-600 text-sm mt-1">Votre pipeline est à jour.</p>
    </div>`}
  </div>`
}

// ══════════════════════════════════════════════════════════════════════════════
//  CONTACTS
// ══════════════════════════════════════════════════════════════════════════════

async function loadContacts(page = 0) {
  const data = await GET(`/contacts?page=${page}&size=20&sort=nom,asc`)
  if (!data) return
  S.contacts = { items: data.content, page: data.number, totalPages: data.totalPages, totalElements: data.totalElements }
}

function renderContacts() {
  const { items, page, totalPages, totalElements } = S.contacts
  return `${pageHeader('Contacts', `${totalElements} contact(s)`, primaryBtn('Nouveau contact', 'openContactModal(null)'))}
  <div class="p-8">
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      ${items.length === 0 ? emptyState('Aucun contact', 'Créez votre premier contact pour commencer à tracker vos opportunités.') : `
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Nom</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Entreprise</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Email</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Canal</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Opps</th>
              <th class="px-5 py-3"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            ${items.map(c => `
            <tr class="hover:bg-gray-50/70 transition-colors">
              <td class="px-5 py-3.5 font-medium text-gray-900">
                ${esc(c.nom)}${c.prenom ? ' ' + esc(c.prenom) : ''}
              </td>
              <td class="px-5 py-3.5 text-gray-600">${esc(c.entreprise || '—')}</td>
              <td class="px-5 py-3.5">
                ${c.email ? `<a href="mailto:${esc(c.email)}" class="text-indigo-600 hover:underline">${esc(c.email)}</a>` : '<span class="text-gray-400">—</span>'}
              </td>
              <td class="px-5 py-3.5 text-gray-600 text-xs">${c.canal ? (CANAL_LABELS[c.canal] || esc(c.canal)) : '—'}</td>
              <td class="px-5 py-3.5">
                <span class="font-semibold text-gray-700">${c.opportunites?.length || 0}</span>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1 justify-end">
                  ${iconBtn('Modifier', `editContact(${c.id})`, editSvg(), 'hover:text-indigo-600 hover:bg-indigo-50')}
                  ${iconBtn('Supprimer', `deleteContact(${c.id})`, trashSvg(), 'hover:text-red-600 hover:bg-red-50')}
                </div>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>
      <div class="px-5 pb-5">${pagBar(page, totalPages, 'navContacts')}</div>`}
    </div>
  </div>`
}

async function navContacts(page) {
  await loadContacts(page); render()
}

function editContact(id) {
  const c = S.contacts.items.find(x => x.id === id)
  if (c) openContactModal(c)
}

async function openContactModal(contact) {
  const e = !!contact
  let entreprises = []
  try { entreprises = await GET('/contacts/entreprises') } catch (_) {}
  const datalistId = 'dl-entreprises'
  const datalistOpts = (entreprises || []).map(n => `<option value="${esc(n)}">`).join('')
  openModal(`<div class="p-6">
    <h2 class="text-lg font-bold text-gray-900 mb-5">${e ? 'Modifier le contact' : 'Nouveau contact'}</h2>
    <form onsubmit="submitContact(event, ${e ? contact.id : 'null'})">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-3">
          ${formField('nom',    'Nom *',    'text', e ? contact.nom    : '', '', true)}
          ${formField('prenom','Prénom',    'text', e ? (contact.prenom || '') : '')}
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Entreprise</label>
          <input name="entreprise" type="text" list="${datalistId}"
            value="${esc(e ? (contact.entreprise || '') : '')}"
            autocomplete="off"
            class="${selectCls()}" placeholder="Nom de l'entreprise">
          <datalist id="${datalistId}">${datalistOpts}</datalist>
        </div>
        ${formField('email',  'Email',    'email', e ? (contact.email     || '') : '')}
        ${formField('telephone','Téléphone','text', e ? (contact.telephone || '') : '')}
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Canal</label>
          <select name="canal" class="${selectCls()}">
            <option value="">— Sélectionner —</option>
            ${Object.entries(CANAL_LABELS).map(([v,l]) => `<option value="${v}" ${e && contact.canal===v?'selected':''}>${l}</option>`).join('')}
          </select>
        </div>
      </div>
      ${modalFooter(e ? 'Enregistrer' : 'Créer le contact')}
    </form>
  </div>`)
}

async function submitContact(ev, id) {
  ev.preventDefault()
  const fd = new FormData(ev.target)
  const body = {
    nom: fd.get('nom'), prenom: fd.get('prenom') || null,
    entreprise: fd.get('entreprise') || null, email: fd.get('email') || null,
    telephone: fd.get('telephone') || null, canal: fd.get('canal') || null,
  }
  try {
    id ? await PUT(`/contacts/${id}`, body) : await POST('/contacts', body)
    toast(id ? 'Contact modifié' : 'Contact créé')
    closeModal()
    await loadContacts(S.contacts.page); render()
  } catch (err) { toast(err.message, 'err') }
}

async function deleteContact(id) {
  if (!confirm('Supprimer ce contact ? Ses opportunités seront aussi supprimées.')) return
  try {
    await DEL(`/contacts/${id}`)
    toast('Contact supprimé')
    await loadContacts(0); render()
  } catch (err) { toast(err.message, 'err') }
}

// ══════════════════════════════════════════════════════════════════════════════
//  OPPORTUNITÉS
// ══════════════════════════════════════════════════════════════════════════════

async function loadOpps(page = 0, statut, type) {
  if (statut !== undefined) S.opps.statut = statut
  if (type   !== undefined) S.opps.type   = type
  const p = new URLSearchParams({ page, size: 20 })
  if (S.opps.statut) p.set('statut', S.opps.statut)
  if (S.opps.type)   p.set('type',   S.opps.type)
  const data = await GET(`/opportunites?${p}`)
  if (!data) return
  S.opps = { ...S.opps, items: data.content, page: data.number, totalPages: data.totalPages, totalElements: data.totalElements }
}

function renderOpportunites() {
  const { items, page, totalPages, totalElements } = S.opps
  return `${pageHeader('Opportunités', `${totalElements} opportunité(s)`, primaryBtn('Nouvelle opportunité', 'openOppModal(null)'))}
  <div class="p-8">
    <!-- Filters -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm px-5 py-3.5 mb-4 flex flex-wrap items-center gap-3">
      <select onchange="filterOpps('statut',this.value)" class="${selectCls('w-auto')}">
        <option value="">Tous les statuts</option>
        ${Object.entries(STATUT_LABELS).map(([v,l]) => `<option value="${v}" ${S.opps.statut===v?'selected':''}>${l}</option>`).join('')}
      </select>
      <select onchange="filterOpps('type',this.value)" class="${selectCls('w-auto')}">
        <option value="">Tous les types</option>
        ${Object.entries(TYPE_LABELS).map(([v,l]) => `<option value="${v}" ${S.opps.type===v?'selected':''}>${l}</option>`).join('')}
      </select>
      ${S.opps.statut || S.opps.type ? `<button onclick="resetOppFilters()" class="text-sm text-gray-500 hover:text-gray-800 underline">Effacer</button>` : ''}
    </div>

    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      ${items.length === 0 ? emptyState('Aucune opportunité', 'Créez votre première opportunité commerciale.') : `
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Titre</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Contact</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Type</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Statut</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Montant</th>
              <th class="text-left px-5 py-3 font-semibold text-gray-500 text-xs uppercase tracking-wide">Dernière action</th>
              <th class="px-5 py-3"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            ${items.map(o => `
            <tr class="hover:bg-gray-50/70 transition-colors">
              <td class="px-5 py-3.5 font-medium text-gray-900 max-w-[200px] truncate">${esc(o.titre)}</td>
              <td class="px-5 py-3.5 text-gray-600">${esc(o.contactNom || '—')}</td>
              <td class="px-5 py-3.5 text-gray-600 text-xs">${TYPE_LABELS[o.type] || esc(o.type)}</td>
              <td class="px-5 py-3.5">${statutBadge(o.statut)}</td>
              <td class="px-5 py-3.5 text-gray-700 font-medium">${montantLabel(o)}</td>
              <td class="px-5 py-3.5 text-gray-500 text-xs">${fmtDate(o.dateDerniereAction)}</td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1 justify-end">
                  ${iconBtn('Statut', `openStatutModal(${o.id},'${o.statut}')`, checkSvg(), 'hover:text-green-600 hover:bg-green-50')}
                  ${iconBtn('Relances', `openRelancesModal(${o.id})`, chatSvg(), 'hover:text-indigo-600 hover:bg-indigo-50')}
                  ${iconBtn('Modifier', `editOpp(${o.id})`, editSvg(), 'hover:text-indigo-600 hover:bg-indigo-50')}
                </div>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>
      <div class="px-5 pb-5">${pagBar(page, totalPages, 'navOpps')}</div>`}
    </div>
  </div>`
}

async function navOpps(page) { await loadOpps(page); render() }

async function filterOpps(key, value) {
  if (key === 'statut') await loadOpps(0, value, undefined)
  else                  await loadOpps(0, undefined, value)
  render()
}

async function resetOppFilters() {
  S.opps.statut = ''; S.opps.type = ''
  await loadOpps(0); render()
}

function editOpp(id) {
  const o = S.opps.items.find(x => x.id === id) || S.aRelancer.find(x => x.id === id)
  if (o) openOppModal(o)
}

async function openOppModal(opp) {
  const e = !!opp
  openModal(`<div class="p-6"><p class="text-sm text-gray-400">Chargement…</p></div>`)
  const cd = await GET('/contacts?size=500&sort=nom,asc').catch(() => null)
  const contacts = cd?.content || []

  openModal(`<div class="p-6">
    <h2 class="text-lg font-bold text-gray-900 mb-5">${e ? 'Modifier l\'opportunité' : 'Nouvelle opportunité'}</h2>
    <form onsubmit="submitOpp(event, ${e ? opp.id : 'null'})">
      <div class="space-y-4">
        ${formField('titre', 'Titre *', 'text', e ? opp.titre : '', '', true)}
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Type *</label>
            <select name="type" required onchange="toggleMontant(this.value)" class="${selectCls()}">
              ${Object.entries(TYPE_LABELS).map(([v,l]) => `<option value="${v}" ${e&&opp.type===v?'selected':''}>${l}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Statut</label>
            <select name="statut" class="${selectCls()}">
              ${Object.entries(STATUT_LABELS).map(([v,l]) => `<option value="${v}" ${e&&opp.statut===v?'selected':(!e&&v==='CONTACTE'?'selected':'')}>${l}</option>`).join('')}
            </select>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Contact</label>
          <select name="contactId" class="${selectCls()}">
            <option value="">— Aucun —</option>
            ${contacts.map(c => `<option value="${c.id}" ${e&&opp.contactId===c.id?'selected':''}>${esc(c.nom)}${c.prenom?' '+esc(c.prenom):''}${c.entreprise?' ('+esc(c.entreprise)+')':''}</option>`).join('')}
          </select>
        </div>
        <div id="f-tjm"     class="${e?(opp.type==='MISSION_FREELANCE'?'':'hidden'):''}">
          ${formField('tjm',    'TJM (€/jour)',       'number', e&&opp.tjm?opp.tjm:'')}
        </div>
        <div id="f-salaire" class="${e?(opp.type==='CDI'?'':'hidden'):'hidden'}">
          ${formField('salaire','Salaire annuel (€)',  'number', e&&opp.salaire?opp.salaire:'')}
        </div>
        <div id="f-budget"  class="${e?(opp.type==='SITE_WEB'?'':'hidden'):'hidden'}">
          ${formField('budget', 'Budget projet (€)',   'number', e&&opp.budget?opp.budget:'')}
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Note</label>
          <textarea name="note" rows="3" class="${selectCls()} resize-none">${e ? esc(opp.note || '') : ''}</textarea>
        </div>
      </div>
      ${modalFooter(e ? 'Enregistrer' : 'Créer l\'opportunité')}
    </form>
  </div>`)

  if (!e) toggleMontant('MISSION_FREELANCE')
}

function toggleMontant(type) {
  document.getElementById('f-tjm')?.classList.toggle('hidden',     type !== 'MISSION_FREELANCE')
  document.getElementById('f-salaire')?.classList.toggle('hidden', type !== 'CDI')
  document.getElementById('f-budget')?.classList.toggle('hidden',  type !== 'SITE_WEB')
}

async function submitOpp(ev, id) {
  ev.preventDefault()
  const fd = new FormData(ev.target)
  const body = {
    titre: fd.get('titre'), type: fd.get('type'), statut: fd.get('statut') || null,
    contactId: fd.get('contactId') ? Number(fd.get('contactId')) : null,
    tjm:     fd.get('tjm')     ? Number(fd.get('tjm'))     : null,
    salaire: fd.get('salaire') ? Number(fd.get('salaire')) : null,
    budget:  fd.get('budget')  ? Number(fd.get('budget'))  : null,
    note:    fd.get('note')    || null,
  }
  try {
    id ? await PUT(`/opportunites/${id}`, body) : await POST('/opportunites', body)
    toast(id ? 'Opportunité modifiée' : 'Opportunité créée')
    closeModal()
    await Promise.all([loadOpps(S.opps.page), loadDashboard()]); render()
  } catch (err) { toast(err.message, 'err') }
}

function openStatutModal(oppId, current) {
  openModal(`<div class="p-6">
    <h2 class="text-lg font-bold text-gray-900 mb-5">Changer le statut</h2>
    <form onsubmit="submitStatut(event,${oppId})">
      <div class="space-y-2 mb-5">
        ${Object.entries(STATUT_LABELS).map(([v,l]) => `
        <label class="flex items-center gap-3 p-3 border rounded-xl cursor-pointer transition-colors ${v===current?'border-indigo-400 bg-indigo-50':'border-gray-200 hover:bg-gray-50'}">
          <input type="radio" name="statut" value="${v}" ${v===current?'checked':''} class="text-indigo-600 focus:ring-indigo-500"/>
          <span class="flex-1 text-sm font-medium text-gray-800">${l}</span>
          ${statutBadge(v)}
        </label>`).join('')}
      </div>
      ${modalFooter('Appliquer le statut')}
    </form>
  </div>`)
}

async function submitStatut(ev, oppId) {
  ev.preventDefault()
  const statut = new FormData(ev.target).get('statut')
  try {
    await PUT(`/opportunites/${oppId}/statut`, { statut })
    toast('Statut mis à jour')
    closeModal()
    await Promise.all([loadOpps(S.opps.page), loadDashboard()]); render()
  } catch (err) { toast(err.message, 'err') }
}

// ══════════════════════════════════════════════════════════════════════════════
//  RELANCES MODAL
// ══════════════════════════════════════════════════════════════════════════════

async function openRelancesModal(oppId) {
  openModal(`<div class="p-6 text-center"><div class="spin w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full mx-auto"></div></div>`)
  try {
    const relances = await GET(`/opportunites/${oppId}/relances`)
    const opp = S.opps.items.find(o => o.id === oppId) || S.aRelancer.find(o => o.id === oppId)
    const titre = opp?.titre || `Opportunité #${oppId}`

    openModal(`<div class="p-6">
      <div class="flex items-start justify-between mb-5">
        <div>
          <h2 class="text-lg font-bold text-gray-900">Relances</h2>
          <p class="text-sm text-gray-500 mt-0.5 truncate max-w-xs">${esc(titre)}</p>
        </div>
        <button onclick="closeModal()" class="text-gray-400 hover:text-gray-600 mt-1">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <!-- History -->
      ${relances.length === 0
        ? `<p class="text-sm text-gray-400 text-center py-4 mb-5">Aucune relance enregistrée.</p>`
        : `<div class="space-y-2 mb-5 max-h-52 overflow-y-auto pr-1">
            ${relances.map(r => `
            <div class="flex items-start gap-3 p-3 bg-gray-50 rounded-xl">
              <div class="flex-1 min-w-0">
                <div class="flex flex-wrap items-center gap-2 mb-1">
                  ${relanceBadge(r.statut)}
                  <span class="text-xs text-gray-400">${fmtDateTime(r.date)}</span>
                </div>
                ${r.note ? `<p class="text-sm text-gray-700">${esc(r.note)}</p>` : ''}
              </div>
            </div>`).join('')}
          </div>`}

      <!-- Add form -->
      <div class="border-t border-gray-100 pt-5">
        <p class="text-sm font-semibold text-gray-700 mb-3">Ajouter une relance</p>
        <form onsubmit="submitRelance(event,${oppId})">
          <div class="space-y-3">
            <div>
              <label class="block text-xs font-medium text-gray-600 mb-1">Note</label>
              <textarea name="note" rows="2" placeholder="Ce qui s'est passé, prochaine étape…"
                class="${selectCls()} resize-none"></textarea>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-medium text-gray-600 mb-1">Statut</label>
                <select name="statut" class="${selectCls()}">
                  ${Object.entries(RELANCE_STATUT_LABELS).map(([v,l]) => `<option value="${v}">${l}</option>`).join('')}
                </select>
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-600 mb-1">Date</label>
                <input name="date" type="date" value="${new Date().toISOString().split('T')[0]}" class="${selectCls()}"/>
              </div>
            </div>
          </div>
          <button type="submit"
            class="w-full mt-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold transition-colors">
            Ajouter la relance
          </button>
        </form>
      </div>
    </div>`)
  } catch (err) { toast(err.message, 'err'); closeModal() }
}

async function submitRelance(ev, oppId) {
  ev.preventDefault()
  const fd = new FormData(ev.target)
  const dateVal = fd.get('date')
  const body = {
    note: fd.get('note') || null,
    statut: fd.get('statut'),
    date: dateVal ? dateVal + 'T00:00:00' : null,
  }
  try {
    await POST(`/opportunites/${oppId}/relances`, body)
    toast('Relance ajoutée ✓')
    closeModal()
    await Promise.all([loadDashboard(), loadOpps(S.opps.page)]); render()
  } catch (err) { toast(err.message, 'err') }
}

// ══════════════════════════════════════════════════════════════════════════════
//  À RELANCER
// ══════════════════════════════════════════════════════════════════════════════

function renderARelancer() {
  const opps = S.aRelancer
  return `${pageHeader('À relancer', `${opps.length} opportunité(s) sans action depuis plus de 7 jours`)}
  <div class="p-8">
    ${opps.length === 0 ? `
    <div class="bg-green-50 border border-green-200 rounded-2xl p-12 text-center">
      <div class="text-5xl mb-4">✓</div>
      <p class="text-xl font-bold text-green-700">Tout est à jour !</p>
      <p class="text-green-600 text-sm mt-2">Aucune opportunité ne nécessite de relance.</p>
    </div>` : `
    <div class="space-y-3">
      ${opps.map(o => `
      <div class="bg-white rounded-2xl border border-orange-200 shadow-sm p-5 flex items-center gap-5">
        <div class="flex-1 min-w-0">
          <div class="flex flex-wrap items-center gap-2 mb-1.5">
            <h3 class="font-semibold text-gray-900">${esc(o.titre)}</h3>
            ${statutBadge(o.statut)}
          </div>
          <div class="flex flex-wrap items-center gap-4 text-sm text-gray-500">
            ${o.contactNom ? `<span>👤 ${esc(o.contactNom)}</span>` : ''}
            <span>${TYPE_LABELS[o.type] || esc(o.type)}</span>
            <span>Dernière action : <strong class="text-gray-700">${fmtDate(o.dateDerniereAction)}</strong></span>
            ${montantLabel(o) !== '—' ? `<span class="font-semibold text-gray-700">${montantLabel(o)}</span>` : ''}
          </div>
        </div>
        <div class="flex items-center gap-2 flex-shrink-0">
          <button onclick="openStatutModal(${o.id},'${o.statut}')"
            class="text-sm border border-gray-300 hover:bg-gray-50 text-gray-700 font-medium px-3 py-2 rounded-xl transition-colors">
            Changer statut
          </button>
          <button onclick="openRelancesModal(${o.id})"
            class="text-sm bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-4 py-2 rounded-xl transition-colors">
            Relancer
          </button>
        </div>
      </div>`).join('')}
    </div>`}
  </div>`
}

// ══════════════════════════════════════════════════════════════════════════════
//  MINI COMPONENT HELPERS
// ══════════════════════════════════════════════════════════════════════════════

function emptyState(title, sub) {
  return `<div class="text-center py-16 text-gray-500">
    <svg class="w-14 h-14 mx-auto mb-3 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
    </svg>
    <p class="font-semibold text-gray-700">${title}</p>
    <p class="text-sm mt-1">${sub}</p>
  </div>`
}

function formField(name, label, type, value, extra = '', required = false) {
  return `<div>
    <label class="block text-sm font-medium text-gray-700 mb-1">${label}</label>
    <input name="${name}" type="${type}" value="${esc(value)}" ${required ? 'required' : ''} ${extra}
      class="${selectCls()}"/>
  </div>`
}

function selectCls(extra = '') {
  return `w-full px-3 py-2 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 ${extra}`
}

function modalFooter(submitLabel) {
  return `<div class="flex gap-3 mt-6">
    <button type="button" onclick="closeModal()"
      class="flex-1 py-2.5 border border-gray-300 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">Annuler</button>
    <button type="submit"
      class="flex-1 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold transition-colors">${submitLabel}</button>
  </div>`
}

function iconBtn(title, onclick, svg, hoverCls) {
  return `<button onclick="${onclick}" title="${title}"
    class="p-1.5 text-gray-400 ${hoverCls} rounded-lg transition-colors">
    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">${svg}</svg>
  </button>`
}

function editSvg()  { return '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>' }
function trashSvg() { return '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>' }
function checkSvg() { return '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>' }
function chatSvg()  { return '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-3 3v-3z"/>' }

// ══════════════════════════════════════════════════════════════════════════════
//  ROUTER + RENDER
// ══════════════════════════════════════════════════════════════════════════════

async function navigate(view) {
  S.view = view
  try {
    if (view === 'dashboard')    await loadDashboard()
    else if (view === 'contacts')     await Promise.all([loadContacts(0), loadDashboard()])
    else if (view === 'opportunites') await Promise.all([loadOpps(0), loadDashboard()])
    else if (view === 'aRelancer')    await loadDashboard()
  } catch (err) {
    toast('Erreur : ' + err.message, 'err')
  }
  render()
}

function render() {
  const app = document.getElementById('app')
  if (!app) return
  if (!S.token) { app.innerHTML = renderAuthPage(); return }
  let content
  switch (S.view) {
    case 'contacts':     content = renderContacts();     break
    case 'opportunites': content = renderOpportunites(); break
    case 'aRelancer':    content = renderARelancer();    break
    default:             content = renderDashboard()
  }
  app.innerHTML = renderLayout(content)
}

// ══════════════════════════════════════════════════════════════════════════════
//  INIT
// ══════════════════════════════════════════════════════════════════════════════

window.addEventListener('DOMContentLoaded', async () => {
  // Savoir si l'onglet Inscription doit être affiché
  try {
    const status = await GET('/auth/status')
    S.inscriptionOuverte = status?.inscriptionOuverte ?? true
  } catch { S.inscriptionOuverte = true }

  if (S.token) {
    try {
      await loadDashboard()
    } catch {
      doLogout(); return
    }
  }
  render()
})
