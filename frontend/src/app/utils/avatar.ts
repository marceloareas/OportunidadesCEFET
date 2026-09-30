export const AVATAR_ALUNO = 'avatar-aluno.svg';
export const AVATAR_PROFESSOR = 'avatar-professor.svg';

export function avatarPadrao(funcao?: string | null): string {
  return funcao?.toLowerCase() === 'professor' ? AVATAR_PROFESSOR : AVATAR_ALUNO;
}

export function avatarOuPadrao(imagem?: string | null, funcao?: string | null): string {
  return imagem && imagem.trim() ? imagem : avatarPadrao(funcao);
}

export function isAvatarPadrao(imagem?: string | null): boolean {
  return !imagem || imagem === AVATAR_ALUNO || imagem === AVATAR_PROFESSOR;
}
