renderShell('people.html');

const personId = new URLSearchParams(location.search).get('id');
const $ = id => document.getElementById(id);
const num = id => { const v = $(id).value.trim(); return v === '' ? null : Number(v); };
const txt = id => { const v = $(id).value.trim(); return v === '' ? null : v; };

const TYPE_LABEL = { GIVEN: 'Money given', TAKEN: 'Money taken', KEPT: 'Kept with me' };
const ENTRY_LABEL = {
  GIVEN: { ADDITION: 'More money given', REDUCTION: 'Money received back' },
  TAKEN: { ADDITION: 'More money taken', REDUCTION: 'Money paid back' },
  KEPT:  { ADDITION: 'Money added',      REDUCTION: 'Money withdrawn' }
};

let data = null;

function el(tag, cls, text) {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  if (text !== undefined) e.textContent = text;
  return e;
}
function button(label, cls, onClick) {
  const b = el('button', 'btn ' + cls, label);
  b.type = 'button';
  b.addEventListener('click', onClick);
  return b;
}
function chip(cls, text) { return el('span', 'chip ' + cls, text); }

async function load() {
  if (!personId) {
    $('error').textContent = 'No person selected.';
    return;
  }
  try {
    data = await api('/api/people/' + personId + '/history');
    render();
  } catch (err) {
    $('error').textContent = 'Could not load this person: ' + err.message;
  }
}

function render() {
  const p = data.person;
  $('pAvatar').textContent = initials(p.name);
  $('pName').textContent = p.name;
  const phone = $('pPhone');
  phone.textContent = p.phone || 'No phone';
  phone.href = p.phone ? 'tel:' + p.phone.replace(/[^0-9+]/g, '') : '#';
  $('pNotes').textContent = p.notes || '';

  let receive = 0, pay = 0, kept = 0;
  data.accounts.forEach(h => {
    if (h.balance <= 0) return;
    if (h.account.type === 'GIVEN') receive += h.balance;
    else if (h.account.type === 'TAKEN') pay += h.balance;
    else kept += h.balance;
  });
  const chips = $('pChips');
  chips.replaceChildren();
  if (receive > 0) chips.append(chip('green', 'To receive ' + money(receive)));
  if (pay > 0) chips.append(chip('red', 'To pay ' + money(pay)));
  if (kept > 0) chips.append(chip('blue', 'Kept ' + money(kept)));

  const box = $('accounts');
  box.replaceChildren();
  if (data.accounts.length === 0) {
    const panel = el('section', 'panel');
    panel.append(el('p', 'empty', 'No accounts yet. Tap "+ New account".'));
    box.append(panel);
    return;
  }
  data.accounts.forEach(h => box.append(accountPanel(h)));
}

function interestText(a) {
  if (a.interestEnabled && a.monthlyRatePercent != null) {
    return 'Interest: ' + a.monthlyRatePercent + '% per ' + (a.interestMode === 'DAILY' ? 'day' : 'month');
  }
  if (a.interestPlan === 'DECIDE_LATER') return 'Interest: decide when money comes back';
  return 'No interest running';
}

function accountPanel(h) {
  const a = h.account;
  const open = h.balance > 0;
  const panel = el('section', 'panel');

  const head = el('div', 'acc-head');
  const left = el('div');
  left.append(el('div', 'name', TYPE_LABEL[a.type]),
    el('div', 'sub', 'Started ' + formatDate(a.startDate) + ' | ' +
      (a.dueDate ? 'Due ' + formatDate(a.dueDate) : 'No date told')));
  const right = el('div', 'side');
  right.append(el('div', 'amount big', money(h.balance)));
  const status = el('div', 'chips');
  status.append(chip(open ? 'blue' : 'green', open ? 'Open' : 'Settled'));
  if (open && a.type === 'GIVEN' && a.dueDate && daysBetween(a.dueDate) > 0) {
    status.append(chip('red', daysBetween(a.dueDate) + ' days late'));
  }
  right.append(status);
  head.append(left, right);
  panel.append(head);

  const meta = el('div', 'meta');
  const lines = ['Original amount: ' + money(a.principal), interestText(a)];
  if (a.guarantorName) lines.push('Guarantor: ' + a.guarantorName);
  if (a.notes) lines.push('Notes: ' + a.notes);
  lines.forEach(t => meta.append(el('div', '', t)));
  panel.append(meta);

  const btns = el('div', 'row-btns');
  btns.append(button('+ Entry', 'light small', () => openEntry(a)));
  if (open) btns.append(button('Money came back', 'green small', () => openSettle(a, h.balance)));
  if (open && a.type === 'GIVEN' && data.person.phone) {
    btns.append(button('WhatsApp reminder', 'light small', () => whatsapp(a, h.balance)));
  }
  btns.append(button('Delete account', 'danger small', () => deleteAccount(a)));
  panel.append(btns);

  const diary = el('div', 'diary');
  if (h.entries.length === 0) {
    diary.append(el('p', 'empty', 'No diary entries yet.'));
  }
  h.entries.forEach(e => {
    const row = el('div', 'diary-row');
    const label = e.note || ENTRY_LABEL[a.type][e.type];
    const sign = e.type === 'ADDITION' ? '+ ' : '- ';
    const del = el('button', 'x', '\u00d7');
    del.type = 'button';
    del.title = 'Delete this entry';
    del.addEventListener('click', () => deleteEntry(e));
    row.append(el('div', 'd', formatDate(e.entryDate)), el('div', 'n', label),
      el('div', 'v', sign + money(e.amount)), del);
    diary.append(row);
  });
  panel.append(diary);
  return panel;
}

// ----- WhatsApp reminder -----
function whatsapp(a, balance) {
  let digits = data.person.phone.replace(/[^0-9]/g, '');
  if (digits.length === 10) digits = '91' + digits;
  const msg = 'Hello ' + data.person.name + ', this is a reminder about ' + money(balance) +
    ' given on ' + formatDate(a.startDate) + '. Please let me know when you can return it. Thank you.';
  window.open('https://wa.me/' + digits + '?text=' + encodeURIComponent(msg), '_blank');
}

// ----- New account -----
$('newAccBtn').addEventListener('click', () => {
  $('accForm').reset();
  $('aStart').value = todayText();
  $('fixedBox').hidden = true;
  $('accDialog').showModal();
});
$('accCancel').addEventListener('click', () => $('accDialog').close());
$('aPlan').addEventListener('change', () => { $('fixedBox').hidden = $('aPlan').value !== 'FIXED'; });

$('accForm').addEventListener('submit', async e => {
  e.preventDefault();
  const plan = $('aPlan').value;
  const acc = {
    person: { id: Number(personId) },
    type: $('aType').value,
    principal: num('aPrincipal'),
    startDate: $('aStart').value,
    dueDate: txt('aDue'),
    guarantorName: txt('aGuarantor'),
    notes: txt('aNotes'),
    interestPlan: plan,
    interestEnabled: plan === 'FIXED'
  };
  if (plan === 'FIXED') {
    if (num('aRate') === null) { toast('Please enter the interest %', true); return; }
    acc.monthlyRatePercent = num('aRate');
    acc.interestMode = $('aMode').value;
    acc.graceMonths = num('aGrace') ?? 0;
    acc.lateRatePercent = num('aLateRate');
    acc.lateMode = $('aLateMode').value;
  }
  try {
    await api('/api/accounts', { method: 'POST', body: JSON.stringify(acc) });
    $('accDialog').close();
    toast('Account saved');
    load();
  } catch (err) {
    toast('Could not save: ' + err.message, true);
  }
});

// ----- Add entry -----
function openEntry(a) {
  $('entryForm').reset();
  const select = $('eType');
  select.replaceChildren();
  Object.entries(ENTRY_LABEL[a.type]).forEach(([value, label]) => {
    const o = el('option', '', label);
    o.value = value;
    select.append(o);
  });
  $('eDate').value = todayText();
  $('entryDialog').dataset.accountId = a.id;
  $('entryDialog').showModal();
}
$('entryCancel').addEventListener('click', () => $('entryDialog').close());

$('entryForm').addEventListener('submit', async e => {
  e.preventDefault();
  const entry = {
    account: { id: Number($('entryDialog').dataset.accountId) },
    type: $('eType').value,
    amount: num('eAmount'),
    entryDate: $('eDate').value,
    note: txt('eNote')
  };
  try {
    await api('/api/entries', { method: 'POST', body: JSON.stringify(entry) });
    $('entryDialog').close();
    toast('Entry saved');
    load();
  } catch (err) {
    toast('Could not save: ' + err.message, true);
  }
});

// ----- Money came back (settle) -----
function openSettle(a, balance) {
  $('settleForm').reset();
  $('sDate').value = todayText();
  $('sCalc').checked = a.interestPlan !== 'NONE';
  $('sRate').value = a.interestEnabled && a.monthlyRatePercent != null ? a.monthlyRatePercent : '';
  $('sMode').value = a.interestMode || 'MONTHLY';
  $('sLateRate').value = a.lateRatePercent != null ? a.lateRatePercent : '';
  $('sLateMode').value = a.lateMode || 'MONTHLY';
  $('sInterestBox').hidden = !$('sCalc').checked;
  $('sPreview').hidden = true;
  $('settleDialog').dataset.accountId = a.id;
  $('settleDialog').dataset.balance = balance;
  $('settleDialog').showModal();
}
$('settleCancel').addEventListener('click', () => $('settleDialog').close());
$('sCalc').addEventListener('change', () => {
  $('sInterestBox').hidden = !$('sCalc').checked;
  $('sPreview').hidden = true;
});

function previewLine(label, value, cls) {
  const d = el('div', cls || '');
  d.append(el('span', '', label), el('span', '', value));
  return d;
}

$('previewBtn').addEventListener('click', async () => {
  const id = $('settleDialog').dataset.accountId;
  const box = $('sPreview');
  box.replaceChildren();
  try {
    if (!$('sCalc').checked) {
      const bal = Number($('settleDialog').dataset.balance);
      box.append(previewLine('Money still owed', money(bal), 'tot'));
    } else {
      const q = new URLSearchParams({ accountId: id, asOf: $('sDate').value });
      if (num('sRate') !== null) q.set('rate', num('sRate'));
      if (num('sLateRate') !== null) q.set('lateRate', num('sLateRate'));
      q.set('interestMode', $('sMode').value);
      q.set('lateMode', $('sLateMode').value);
      const s = await api('/api/statement?' + q);
      box.append(previewLine('Money still owed', money(s.principalBalance)),
        previewLine('Interest', money(s.interest)),
        previewLine('Late charge', money(s.lateCharge)),
        previewLine('Total due', money(s.totalDue), 'tot'));
    }
    box.hidden = false;
  } catch (err) {
    toast('Could not calculate: ' + err.message, true);
  }
});

$('settleForm').addEventListener('submit', async e => {
  e.preventDefault();
  const calc = $('sCalc').checked;
  const body = {
    date: $('sDate').value,
    amountPaid: num('sPaid'),
    skipInterest: !calc,
    ratePercent: calc ? num('sRate') : null,
    interestMode: calc && num('sRate') !== null ? $('sMode').value : null,
    lateRatePercent: calc ? num('sLateRate') : null,
    lateMode: calc && num('sLateRate') !== null ? $('sLateMode').value : null
  };
  try {
    const r = await api('/api/accounts/' + $('settleDialog').dataset.accountId + '/settle',
      { method: 'POST', body: JSON.stringify(body) });
    $('settleDialog').close();
    toast(r.status === 'SETTLED' ? 'Settled fully' : 'Saved. Remaining ' + money(r.remainingBalance));
    load();
  } catch (err) {
    toast('Could not save: ' + err.message, true);
  }
});

// ----- Deletes -----
async function deleteEntry(entry) {
  if (!confirm('Delete this entry?')) return;
  try {
    await api('/api/entries/' + entry.id, { method: 'DELETE' });
    toast('Entry deleted');
    load();
  } catch (err) {
    toast('Could not delete: ' + err.message, true);
  }
}

async function deleteAccount(a) {
  if (!confirm('Delete this account and all its entries? This cannot be undone.')) return;
  try {
    await api('/api/accounts/' + a.id, { method: 'DELETE' });
    toast('Account deleted');
    load();
  } catch (err) {
    toast('Could not delete: ' + err.message, true);
  }
}

$('delPersonBtn').addEventListener('click', async () => {
  if (data.accounts.length > 0) {
    toast('Delete this person\'s accounts first', true);
    return;
  }
  if (!confirm('Delete ' + data.person.name + '?')) return;
  try {
    await api('/api/people/' + personId, { method: 'DELETE' });
    location.href = 'people.html';
  } catch (err) {
    toast('Could not delete: ' + err.message, true);
  }
});

load();