// Format a number as Indian rupees
const money = n => new Intl.NumberFormat('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 2
}).format(n);

// Whole days between a date text (yyyy-mm-dd) and today
const daysSince = dateText =>
  Math.floor((Date.now() - new Date(dateText).getTime()) / 86400000);

async function loadDashboard() {
  const errorBox = document.getElementById('error');
  try {
    const res = await fetch('/api/dashboard');
    if (!res.ok) {
      throw new Error('Server returned ' + res.status);
    }
    const d = await res.json();

    document.getElementById('totalGiven').textContent = money(d.totalGiven);
    document.getElementById('totalTaken').textContent = money(d.totalTaken);
    document.getElementById('totalKept').textContent = money(d.totalKept);

    fillTable('overdueBody', d.overdue, 'dueDate', 'Nobody is overdue.');
    fillTable('toCallBody', d.toCall, 'startDate', 'No one to call.');
  } catch (err) {
    errorBox.textContent = 'Could not load the dashboard: ' + err.message;
  }
}

function fillTable(bodyId, rows, dateField, emptyText) {
  const body = document.getElementById(bodyId);
  body.innerHTML = '';

  if (rows.length === 0) {
    const tr = document.createElement('tr');
    const td = document.createElement('td');
    td.colSpan = 5;
    td.className = 'empty';
    td.textContent = emptyText;
    tr.appendChild(td);
    body.appendChild(tr);
    return;
  }

  rows.forEach(r => {
    const tr = document.createElement('tr');

    const nameCell = document.createElement('td');
    const link = document.createElement('a');
    link.href = 'person.html?id=' + r.personId;      // person page comes in the next step
    link.textContent = r.personName;
    nameCell.appendChild(link);

    const values = [
      r.phone || '-',
      r[dateField],
      daysSince(r[dateField]),
      money(r.balance)
    ];

    tr.appendChild(nameCell);
    values.forEach(v => {
      const td = document.createElement('td');
      td.textContent = v;
      tr.appendChild(td);
    });
    body.appendChild(tr);
  });
}

loadDashboard();