document.addEventListener('DOMContentLoaded', () => {
  const corpo = document.getElementById('itens-corpo');
  const modelo = document.getElementById('linha-modelo');
  const total = document.getElementById('total');

  function reindexar() {
    corpo.querySelectorAll('tr').forEach((linha, i) => {
      linha.querySelectorAll('input').forEach((campo) => {
        campo.name = campo.name.replace(/itens\[\d+\]/, 'itens[' + i + ']');
        campo.id = campo.name.replace(/[\[\].]/g, '_');
      });
    });
    atualizarTotal();
  }

  function atualizarTotal() {
    let soma = 0;
    corpo.querySelectorAll('tr').forEach((linha) => {
      const qtd = parseFloat(linha.querySelector('input[name$=".quantidade"]').value) || 0;
      const valor = parseFloat(linha.querySelector('input[name$=".valorUnitario"]').value) || 0;
      soma += qtd * valor;
    });
    total.textContent = soma.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  document.getElementById('add-item').addEventListener('click', () => {
    corpo.insertAdjacentHTML('beforeend', modelo.innerHTML.replaceAll('__INDEX__', corpo.children.length));
    reindexar();
  });

  corpo.addEventListener('click', (evento) => {
    if (!evento.target.classList.contains('remover')) return;
    if (corpo.children.length > 1) {
      evento.target.closest('tr').remove();
      reindexar();
    }
  });

  corpo.addEventListener('input', atualizarTotal);
  atualizarTotal();
});
