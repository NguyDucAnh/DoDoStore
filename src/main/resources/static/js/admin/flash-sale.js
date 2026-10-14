(() => {
 const form=document.getElementById('campaign-form');if(!form)return;
 const picker=document.getElementById('product-picker'),allNote=document.getElementById('all-products-note');
 const boxes=[...form.querySelectorAll('[name="productIds"]')],options=[...form.querySelectorAll('.product-option')];
 const error=document.getElementById('campaign-form-error'),count=document.getElementById('selection-count');
 function update(){const all=form.querySelector('[name="scope"]:checked')?.value==='ALL';picker.hidden=all;allNote.hidden=!all;boxes.forEach(b=>b.disabled=all);count.textContent=boxes.filter(b=>b.checked).length+' sản phẩm được chọn';error.textContent='';}
 form.querySelectorAll('[name="scope"],[name="productIds"]').forEach(input=>input.addEventListener('change',update));
 const normalize=s=>s.normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/g,'d').replace(/Đ/g,'D').toLowerCase();
 document.getElementById('product-search').addEventListener('input',e=>{const query=normalize(e.target.value.trim());options.forEach(row=>row.hidden=!normalize(row.dataset.search).includes(query));document.getElementById('no-search-result').hidden=options.some(row=>!row.hidden);});
 document.getElementById('select-visible').addEventListener('click',()=>{options.filter(row=>!row.hidden).forEach(row=>row.querySelector('input').checked=true);update();});
 document.getElementById('clear-selection').addEventListener('click',()=>{boxes.forEach(b=>b.checked=false);update();});
 form.addEventListener('submit',e=>{const selected=form.querySelector('[name="scope"]:checked')?.value==='SELECTED';if(selected&&!boxes.some(b=>b.checked)){e.preventDefault();error.textContent='Vui lòng chọn ít nhất một sản phẩm.';return;}
 const start=form.elements.startsAt.value,end=form.elements.endsAt.value;if(end<=start){e.preventDefault();error.textContent='Thời gian kết thúc phải sau thời gian bắt đầu.';return;}});
 document.querySelectorAll('[data-stop-campaign]').forEach(f=>f.addEventListener('submit',e=>{if(!confirm('Dừng chiến dịch và bỏ ưu đãi của các sản phẩm còn thuộc chiến dịch này?'))e.preventDefault();}));update();
})();
