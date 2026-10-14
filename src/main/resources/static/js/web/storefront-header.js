(() => {
 const root=document.querySelector('.storefront-original'); if(!root || root.dataset.ready)return; root.dataset.ready='true';
 if(location.pathname==='/home'||location.pathname==='/home/')root.querySelectorAll('a[href^="/home#"]').forEach(link=>link.setAttribute('href',link.getAttribute('href').slice(5)));
 const menu=root.querySelector('[data-store-menu]'), nav=root.querySelector('#store-navigation'), account=root.querySelector('[data-store-account]');
 if(menu)menu.addEventListener('click',()=>{const open=nav.classList.toggle('show');menu.setAttribute('aria-expanded',String(open));});
 if(account)account.addEventListener('click',e=>{e.preventDefault();e.stopPropagation();const open=account.nextElementSibling.classList.toggle('show');account.setAttribute('aria-expanded',String(open));});
 document.addEventListener('click',e=>{if(account&&!account.parentElement.contains(e.target)){account.nextElementSibling.classList.remove('show');account.setAttribute('aria-expanded','false');}if(menu&&!nav.contains(e.target)&&!menu.contains(e.target)){nav.classList.remove('show');menu.setAttribute('aria-expanded','false');}});
 root.addEventListener('keydown',e=>{if(e.key==='Escape'){if(account){account.nextElementSibling.classList.remove('show');account.setAttribute('aria-expanded','false');}if(menu){nav.classList.remove('show');menu.setAttribute('aria-expanded','false');}}});
})();
