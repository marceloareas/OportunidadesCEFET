import { ehAdmin, tipoUsuarioDe } from './perfil';
import { deslogar, logarComo } from '../../testing/test-providers';

describe('perfil do usuário logado', () => {
  afterEach(() => deslogar());

  it('traduz a função do backend para o tipo usado nas telas', () => {
    expect(tipoUsuarioDe('PROFESSOR')).toBe('professor');
    expect(tipoUsuarioDe('ADMIN')).toBe('admin');
    expect(tipoUsuarioDe('ALUNO')).toBe('aluno');
    expect(tipoUsuarioDe(undefined)).toBe('aluno');
  });

  it('reconhece o administrador logado', () => {
    logarComo({ id: 'adm', nome: 'Administrador', funcao: 'ADMIN' });
    expect(ehAdmin()).toBeTrue();
  });

  it('aluno e visitante não são administradores', () => {
    expect(ehAdmin()).toBeFalse();
    logarComo({ id: 'u1', nome: 'Bia', funcao: 'ALUNO' });
    expect(ehAdmin()).toBeFalse();
  });
});
