// data de hoje no fuso do aparelho, no formato AAAA-MM-DD
export function hojeLocal(): string {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

// "30102027" vira "30/10/2027" (aceita só números, até 8 dígitos)
export function mascararData(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 8);
  if (d.length <= 2) return d;
  if (d.length <= 4) return `${d.slice(0, 2)}/${d.slice(2)}`;
  return `${d.slice(0, 2)}/${d.slice(2, 4)}/${d.slice(4)}`;
}

// "30/10/2027" vira "2027-10-30"; devolve null se não for uma data real
export function dataBrParaIso(valor: string): string | null {
  const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(valor);
  if (!m) return null;
  const dia = Number(m[1]);
  const mes = Number(m[2]);
  const ano = Number(m[3]);
  if (ano < 1990 || ano > 2100) return null;
  const d = new Date(ano, mes - 1, dia);
  if (d.getFullYear() !== ano || d.getMonth() !== mes - 1 || d.getDate() !== dia) return null;
  return `${m[3]}-${m[2]}-${m[1]}`;
}

// "2027-10-30" vira "30/10/2027"
export function isoParaDataBr(iso: string | null | undefined): string {
  const m = iso ? /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso) : null;
  return m ? `${m[3]}/${m[2]}/${m[1]}` : '';
}