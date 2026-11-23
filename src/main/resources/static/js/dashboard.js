renderShell('index.html');

async function loadDashboard() {
  try {
    const d = await api('/api/dashboard');
    document.getElementById('totalGiven').textContent = money(d.totalGiven);
    document.getElementById('totalTaken').textContent = money(d.totalTaken);
    document.getElementById('totalKept').textContent = money(d.totalKept);
    document.getElementById('overdueCount').textContent = d.overdue.length;
    document.getElementById('toCallCount').textContent = d.toCall.length;

    fillList('overdueList', d.overdue,
      r => 'Due ' + formatDate(r.dueDate) + ' | ' + daysBetween(r.dueDate) + ' days late',
      'Nobody is overdue.');
    fillList('toCallList', d.toCall,
      r => 'Given ' + formatDate(r.startDate) + ' | ' + daysBetween(r.startDate) + ' days ago',
      'No one to call.');
  } catch (err) {
    document.getElementById('error').textContent = 'Could not load the dashboard: ' + err.message;
  }
}

function fillList(id, rows, subtitle, emptyText) {
  const list = document.getElementById(id);
  list.replaceChildren();

  if (rows.length === 0) {
    const p = document.createElement('p');
    p.className = 'empty';
    p.textContent = emptyText;
    list.appendChild(p);
    return;
  }

  rows.forEach(r => {
    const item = div('item');
    item.addEventListener('click', () => { location.href = 'person.html?id=' + r.personId; });

    const info = div('info');
    info.append(div('name', r.personName), div('sub', subtitle(r)));

    const side = div('side');
    side.append(div('amount', money(r.balance)));
    if (r.phone) {
      const call = document.createElement('a');
      call.className = 'call';
      call.href = 'tel:' + r.phone.replace(/[^0-9+]/g, '');
      call.textContent = 'Call';
      call.addEventListener('click', e => e.stopPropagation());
      side.append(call);
    }

    item.append(div('avatar', initials(r.personName)), info, side);
    list.appendChild(item);
  });
}

loadDashboard();