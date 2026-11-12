// Simula o GET /guias/{id}/linha-do-tempo: junta a guia, a pré-guia que a originou
// e os procedimentos. No mock, o id da guia é o mesmo da pré-guia emitida.
import { banco, copiar } from './bancoMock.js'
import { erroHttp, esperar } from './auxiliares.js'

export async function mockLinhaDoTempoDaGuia(guiaId) {
  await esperar(250)
  const db = banco()
  const guia = db.preGuias.find(p => p.id === Number(guiaId) && p.status === 'EMITIDA')
  if (!guia) {
    throw erroHttp(404, { codigo: 'GUIA_NAO_ENCONTRADA', mensagem: 'Guia não encontrada.' })
  }
  const atendimentos = guia.procedimentos.map(p => p.atendimentoId)

  const eventos = db.historico.filter(
    h =>
      (h.entidadeTipo === 'GUIA' && h.entidadeId === guia.id) ||
      (h.entidadeTipo === 'PRE_GUIA' && h.entidadeId === guia.id) ||
      (h.entidadeTipo === 'GUIA_PROC' && atendimentos.includes(h.entidadeId))
  )
  return copiar(eventos.sort((a, b) => a.data.localeCompare(b.data)))
}