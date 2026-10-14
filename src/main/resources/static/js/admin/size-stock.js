document.querySelectorAll('.size-stock-editor').forEach(editor => {
 const form = editor.closest('form'), sizes = form.querySelector('[name="sizes"]'), colors = form.querySelector('[name="color"]');
 const hidden = editor.querySelector('[name="variantStock"]'), legacy = editor.querySelector('[name="sizeStock"]'), rows = editor.querySelector('[data-size-stock-rows]');
 let values = {};
 try { values = JSON.parse(hidden.value || '{}'); } catch (_) {}
 const old = Object.fromEntries((legacy.value || '').split(',').filter(Boolean).map(v => v.split(':')));
 const list = (text, upper = false) => [...new Set(text.split(',').map(v => upper ? v.trim().toUpperCase() : v.trim().normalize('NFC')).filter(Boolean))];
 // A single-color product has an unambiguous legacy allocation.
 const initialColors = list(colors.value);
 if (!Object.keys(values).length && initialColors.length === 1) values[initialColors[0]] = old;
 function normalize(input) { if (/^[0-9]+$/.test(input.value)) input.value = input.value.replace(/^0+(?=[0-9])/, ''); }
 function sync() {
  const current = {}, totals = {};
  rows.querySelectorAll('input').forEach(input => {
   (current[input.dataset.color] ||= {})[input.dataset.size] = Number(input.value || 0);
   totals[input.dataset.size] = (totals[input.dataset.size] || 0) + Number(input.value || 0);
  });
  hidden.value = JSON.stringify(current);
  legacy.value = Object.entries(totals).map(([s,q]) => s + ':' + q).join(',');
  const total = form.querySelector('[data-stock-total]');
  if (total) total.value = Object.values(totals).reduce((a,b) => a+b,0);
 }
 function render() {
  rows.querySelectorAll('input').forEach(i => { (values[i.dataset.color] ||= {})[i.dataset.size] = i.value; });
  rows.replaceChildren();
  list(colors.value).forEach(color => {
   const group = document.createElement('section'); group.className = 'variant-stock-group';
   const title = document.createElement('h4'); title.textContent = color; group.append(title);
   const grid = document.createElement('div'); grid.className = 'variant-stock-grid';
   list(sizes.value,true).forEach(size => {
    const label = document.createElement('label'); label.className = 'stock-tile'; label.textContent = size;
    const input = document.createElement('input'); input.type='number'; input.min='0'; input.step='1'; input.required=true;
    input.dataset.color=color; input.dataset.size=size; input.value=values[color]?.[size] ?? '0'; input.className='stock-input';
    input.addEventListener('input',sync);
    const commit = () => { normalize(input); sync(); };
    input.addEventListener('change',commit); input.addEventListener('blur',commit);
    label.append(input); grid.append(label);
   });
   group.append(grid); rows.append(group);
  });
  sync();
 }
 sizes.addEventListener('change',render); colors.addEventListener('change',render);
 form.addEventListener('submit',() => { rows.querySelectorAll('input').forEach(normalize); sync(); });
 render();
});
