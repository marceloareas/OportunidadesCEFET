import { TipoNotificacao } from '../services/notificacao.service';

export function rotuloContagem(total: number): string {
  if (!total || total <= 0) return '';
  return total > 9 ? '9+' : String(total);
}

export function tempoRelativo(data: string | number, agora: Date = new Date()): string {
  const minutos = Math.floor((agora.getTime() - new Date(data).getTime()) / 60000);
  if (minutos < 1) return 'agora';
  if (minutos < 60) return `há ${minutos} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `há ${horas} h`;
  const dias = Math.floor(horas / 24);
  return dias === 1 ? 'há 1 dia' : `há ${dias} dias`;
}

export function rotaPorTipo(tipo: TipoNotificacao): string {
  return tipo === 'COMENTARIO_RECEBIDO' ? '/home' : '/oportunidades';
}

const ICONES: Record<TipoNotificacao, string> = {
  CANDIDATURA_RECEBIDA: 'bi-person-plus',
  CANDIDATURA_APROVADA: 'bi-check-circle',
  OPORTUNIDADE_FINALIZADA: 'bi-flag',
  COMENTARIO_RECEBIDO: 'bi-chat-dots',
};

export function iconePorTipo(tipo: TipoNotificacao): string {
  return ICONES[tipo] ?? 'bi-bell';
}
