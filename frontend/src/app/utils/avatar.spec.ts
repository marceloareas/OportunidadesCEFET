import { AVATAR_ALUNO, AVATAR_PROFESSOR, avatarOuPadrao, avatarPadrao, isAvatarPadrao } from './avatar';

describe('utils/avatar', () => {
  it('avatarPadrao escolhe pelo perfil, sem diferenciar maiúsculas', () => {
    expect(avatarPadrao('PROFESSOR')).toBe(AVATAR_PROFESSOR);
    expect(avatarPadrao('professor')).toBe(AVATAR_PROFESSOR);
    expect(avatarPadrao('ALUNO')).toBe(AVATAR_ALUNO);
    expect(avatarPadrao(undefined)).toBe(AVATAR_ALUNO);
    expect(avatarPadrao(null)).toBe(AVATAR_ALUNO);
  });

  it('avatarOuPadrao usa a foto quando existe', () => {
    expect(avatarOuPadrao('data:image/jpeg;base64,AAA', 'ALUNO')).toBe('data:image/jpeg;base64,AAA');
    expect(avatarOuPadrao('', 'PROFESSOR')).toBe(AVATAR_PROFESSOR);
    expect(avatarOuPadrao(undefined, 'ALUNO')).toBe(AVATAR_ALUNO);
  });

  it('isAvatarPadrao reconhece as imagens padrão e a ausência de foto', () => {
    expect(isAvatarPadrao(AVATAR_ALUNO)).toBeTrue();
    expect(isAvatarPadrao(AVATAR_PROFESSOR)).toBeTrue();
    expect(isAvatarPadrao(undefined)).toBeTrue();
    expect(isAvatarPadrao('data:image/jpeg;base64,AAA')).toBeFalse();
  });
});
