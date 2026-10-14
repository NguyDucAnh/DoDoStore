(() => {
 const labels = [...document.querySelectorAll('[data-flash-end]')];
 function tick() { labels.forEach(el => {
  const left = Math.max(0, Math.floor((new Date(el.dataset.flashEnd + '+07:00').getTime() - Date.now()) / 1000));
  if (left === 0) { el.textContent='Flash sale đã kết thúc. Tải lại trang để xem giá mới.'; return; }
  const h=Math.floor(left/3600), m=Math.floor(left%3600/60), s=left%60;
  el.textContent='Còn '+h+' giờ '+m+' phút '+s+' giây';
 }); } tick(); if(labels.length) setInterval(tick,1000);
})();
