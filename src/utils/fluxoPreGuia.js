// O back não tem número de guia: o front mostra o id da guia emitida formatado.
export function numeroGuia(id) {
  return `GE ${String(id).padStart(3, '0')}`
}