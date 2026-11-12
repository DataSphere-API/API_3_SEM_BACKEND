import { api } from './http.js'
import { mockLinhaDoTempoDaGuia } from '../mocks/linhaDoTempoMock.js'
import { erroDeFormato, traduzirErro } from '../utils/erros.js'

const USAR_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// Aceita uma lista pura ou { eventos: [...] }. Ajuste aqui se a US007-2 usar outro formato.
function normalizar(dados) {
  const lista = Array.isArray(dados) ? dados : dados?.eventos
  if (!Array.isArray(lista)) throw erroDeFormato()

  return lista
    .map((e, indice) => ({
      id: e.id ?? indice,
      entidadeTipo: e.entidadeTipo ?? '',
      entidadeId: e.entidadeId ?? null,
      statusAnterior: e.statusAnterior ?? null,
      statusNovo: e.statusNovo ?? '',
      data: e.data ?? null,
      origem: e.origem ?? ''
    }))
    .sort((a, b) => String(a.data ?? '').localeCompare(String(b.data ?? '')))
}

// GET /guias/{id}/linha-do-tempo (US007-2): o id é o da guia emitida.
export async function linhaDoTempoDaGuia(guiaId) {
  try {
    const dados = USAR_MOCK
      ? await mockLinhaDoTempoDaGuia(guiaId)
      : (await api.get(`/guias/${guiaId}/linha-do-tempo`)).data
    return normalizar(dados)
  } catch (erro) {
    throw traduzirErro(erro, { 404: 'Guia não encontrada.' })
  }
}