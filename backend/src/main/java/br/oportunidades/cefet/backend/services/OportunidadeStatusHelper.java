package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.StatusOportunidade;
import br.oportunidades.cefet.backend.models.Oportunidade;

import java.util.Date;

final class OportunidadeStatusHelper {

    private OportunidadeStatusHelper() {
    }

    static StatusOportunidade calcularStatus(Oportunidade oportunidade) {
        if (oportunidade == null) {
            return null;
        }

        if (Boolean.TRUE.equals(oportunidade.getFinalizada())) {
            return StatusOportunidade.FINALIZADA;
        }

        Date inicio = oportunidade.getDataInicioInscricao();
        Date fim = oportunidade.getDataFimInscricao();
        Date agora = new Date();

        if (inicio == null || fim == null) {
            return StatusOportunidade.INSCRICOES_ABERTAS;
        }

        if (agora.before(inicio)) {
            return StatusOportunidade.INSCRICOES_EM_BREVE;
        }

        if (agora.after(fim)) {
            return StatusOportunidade.INSCRICOES_ENCERRADAS;
        }

        return StatusOportunidade.INSCRICOES_ABERTAS;
    }

    static void aplicarStatus(Oportunidade oportunidade) {
        if (oportunidade != null) {
            oportunidade.setStatus(calcularStatus(oportunidade));
        }
    }

    static boolean estaComInscricoesAbertas(Oportunidade oportunidade) {
        return calcularStatus(oportunidade) == StatusOportunidade.INSCRICOES_ABERTAS;
    }
}
