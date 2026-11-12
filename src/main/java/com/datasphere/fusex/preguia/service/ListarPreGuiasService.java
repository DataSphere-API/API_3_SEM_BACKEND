private PreGuiaResumoResponse montarResumo(PreGuiaModel preGuia) {
    List<PreGuiaProcModel> procedimentos = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuia.getId());

    return new PreGuiaResumoResponse(
            preGuia.getId(),
            preGuia.getBeneficiarioModel().getNome(),
            preGuia.getStatus(),
            procedimentos.size(),
            calcularValorTotalPreGuiaService.calcularValorTotal(procedimentos),
            preGuia.getDataCriacao(),
            preGuia.getDataAprovacaoMedico() != null,
            preGuia.getDataAprovacaoChefe() != null,
            buscarDataEmissaoDaPreGuiaService.buscarDataEmissao(preGuia),
            preGuia.getGuiaId());
}