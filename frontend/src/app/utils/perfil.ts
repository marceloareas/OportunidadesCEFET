export type TipoUsuario = 'aluno' | 'professor' | 'admin';

// Função que vem do backend (ALUNO, PROFESSOR, ADMIN) → tipo guardado em localStorage('tipoUsuario').
export function tipoUsuarioDe(funcao?: string | null): TipoUsuario {
  switch (funcao?.toUpperCase()) {
    case 'PROFESSOR':
      return 'professor';
    case 'ADMIN':
      return 'admin';
    default:
      return 'aluno';
  }
}

// Administrador logado (sprint 6). Lê a função do usuário da sessão, não o tipo, que é só cache de tela.
export function ehAdmin(): boolean {
  if (typeof window === 'undefined') return false;
  try {
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    return usuario?.funcao?.toUpperCase() === 'ADMIN';
  } catch {
    return false;
  }
}
