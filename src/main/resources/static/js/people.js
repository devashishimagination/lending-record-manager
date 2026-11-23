renderShell('people.html');

let allPeople = [];
const dialog = document.getElementById('personDialog');
const form = document.getElementById('personForm');

async function loadPeople() {
  try {
    allPeople = await api('/api/people/summary');
    renderPeople();
  } catch (err) {
    document.getElementById('error').textContent = 'Could not load people: ' + err.message;
  }
}

function chip(cls, text) {
  const c = document.createElement('span');
  c.className = 'chip ' + cls;
  c.textContent = text;
  return c;
}

function renderPeople() {
  const q = document.getElementById('search').value.trim().toLowerCase();
  const rows = allPeople.filter(p =>
    p.name.toLowerCase().includes(q) || (p.phone || '').includes(q));

  const list = document.getElementById('peopleList');
  list.replaceChildren();

  if (rows.length === 0) {
    const p = document.createElement('p');
    p.className = 'empty';
    p.textContent = allPeople.length === 0 ? 'No one added yet. Tap "Add person".' : 'No match found.';
    list.appendChild(p);
    return;
  }

  rows.forEach(p => {
    const item = div('item');
    item.addEventListener('click', () => { location.href = 'person.html?id=' + p.id; });

    const info = div('info');
    info.append(div('name', p.name), div('sub', p.phone || 'No phone'));

    const chips = div('chips');
    if (p.toReceive > 0) chips.append(chip('green', 'To receive ' + money(p.toReceive)));
    if (p.toPay > 0) chips.append(chip('red', 'To pay ' + money(p.toPay)));
    if (p.kept > 0) chips.append(chip('blue', 'Kept ' + money(p.kept)));
    if (p.openAccounts === 0) chips.append(chip('', 'No open accounts'));
    info.append(chips);

    item.append(div('avatar', initials(p.name)), info);
    list.appendChild(item);
  });
}

document.getElementById('search').addEventListener('input', renderPeople);
document.getElementById('addBtn').addEventListener('click', () => { form.reset(); dialog.showModal(); });
document.getElementById('cancelBtn').addEventListener('click', () => dialog.close());

form.addEventListener('submit', async e => {
  e.preventDefault();
  const person = {
    name: document.getElementById('name').value.trim(),
    phone: document.getElementById('phone').value.trim(),
    notes: document.getElementById('notes').value.trim()
  };
  try {
    await api('/api/people', { method: 'POST', body: JSON.stringify(person) });
    dialog.close();
    toast('Person added');
    loadPeople();
  } catch (err) {
    toast('Could not save: ' + err.message, true);
  }
});

loadPeople();