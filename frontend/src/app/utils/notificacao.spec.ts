import { iconePorTipo, rotaPorTipo, rotuloContagem, tempoRelativo } from './notificacao';

describe('utils/notificacao', () => {
  it('rotuloContagem esconde zero e limita em 9+', () => {
    expect(rotuloContagem(0)).toBe('');
    expect(rotuloContagem(3)).toBe('3');
    expect(rotuloContagem(9)).toBe('9');
    expect(rotuloContagem(10)).toBe('9+');
  });

  it('tempoRelativo formata em português', () => {
    const agora = new Date('2026-09-30T12:00:00Z');
    expect(tempoRelativo('2026-09-30T11:59:40Z', agora)).toBe('agora');
    expect(tempoRelativo('2026-09-30T11:55:00Z', agora)).toBe('há 5 min');
    expect(tempoRelativo('2026-09-30T09:00:00Z', agora)).toBe('há 3 h');
    expect(tempoRelativo('2026-09-29T12:00:00Z', agora)).toBe('há 1 dia');
    expect(tempoRelativo('2026-09-27T12:00:00Z', agora)).toBe('há 3 dias');
  });

  it('rotaPorTipo leva comentários para /home e o resto para /oportunidades', () => {
    expect(rotaPorTipo('COMENTARIO_RECEBIDO')).toBe('/home');
    expect(rotaPorTipo('CANDIDATURA_RECEBIDA')).toBe('/oportunidades');
    expect(rotaPorTipo('CANDIDATURA_APROVADA')).toBe('/oportunidades');
    expect(rotaPorTipo('OPORTUNIDADE_FINALIZADA')).toBe('/oportunidades');
  });

  it('iconePorTipo devolve o ícone Bootstrap de cada tipo', () => {
    expect(iconePorTipo('CANDIDATURA_RECEBIDA')).toBe('bi-person-plus');
    expect(iconePorTipo('CANDIDATURA_APROVADA')).toBe('bi-check-circle');
    expect(iconePorTipo('OPORTUNIDADE_FINALIZADA')).toBe('bi-flag');
    expect(iconePorTipo('COMENTARIO_RECEBIDO')).toBe('bi-chat-dots');
  });
});
