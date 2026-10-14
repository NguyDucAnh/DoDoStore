(() => {
  const dialog = document.getElementById('receiptDialog');
  const yes = document.getElementById('receiptYes');
  let selectedForm = null;
  document.querySelectorAll('.receipt-open').forEach(button => {
    button.addEventListener('click', () => {
      selectedForm = button.closest('form');
      document.getElementById('receiptOrderId').textContent = button.dataset.orderId;
      yes.disabled = false;
      dialog.showModal();
    });
  });
  document.getElementById('receiptNo').addEventListener('click', () => dialog.close());
  dialog.addEventListener('close', () => { selectedForm = null; });
  yes.addEventListener('click', () => {
    if (!selectedForm || yes.disabled) return;
    yes.disabled = true;
    selectedForm.submit();
  });
})();
