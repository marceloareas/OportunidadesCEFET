import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';

import { Moderacao } from './moderacao';
import { AdminService } from '../../services/admin.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';
import { Usuario } from '../../services/usuario.service';
import { Page } from '../../services/post.services';
import { deslogar, logarComo, providersDeTeste } from '../../../testing/test-providers';

describe('Moderacao', () => {
  let fixture: ComponentFixture<Moderacao>;
  let admin: jasmine.SpyObj<AdminService>;
  let confirm: jasmine.SpyObj<ConfirmDialogService>;
  let feedback: jasmine.SpyObj<FeedbackService>;

  const pagina = (content: Usuario[], totalPages = 1): Page<Usuario> =>
    ({ content, totalPages, totalElements: content.length, number: 0, size: 10 });

  const bia: Usuario = { id: 'u1', nome: 'Bia', email: 'bia@cefet.br', funcao: 'ALUNO', matricula: '123', ativo: true };
  const adm: Usuario = { id: 'adm', nome: 'Administrador', email: 'admin@cefet-rj.br', funcao: 'ADMIN', ativo: true };

  async function montar(usuarios: Usuario[]) {
    admin = jasmine.createSpyObj<AdminService>('AdminService', ['listarUsuarios', 'desativar', 'reativar']);
    admin.listarUsuarios.and.returnValue(of(pagina(usuarios)));
    confirm = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    feedback = jasmine.createSpyObj<FeedbackService>('FeedbackService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [Moderacao],
      providers: [
        ...providersDeTeste(),
        { provide: AdminService, useValue: admin },
        { provide: ConfirmDialogService, useValue: confirm },
        { provide: FeedbackService, useValue: feedback },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Moderacao);
    fixture.detectChanges();
  }

  const linhas = () => Array.from(fixture.nativeElement.querySelectorAll('tbody tr')) as HTMLElement[];
  const botaoDe = (linha: HTMLElement) => linha.querySelector('button') as HTMLButtonElement | null;

  beforeEach(() => logarComo({ id: 'adm', nome: 'Administrador', funcao: 'ADMIN' }));
  afterEach(() => deslogar());

  it('lista os usuários com e-mail, perfil e situação', async () => {
    await montar([adm, bia]);

    expect(admin.listarUsuarios).toHaveBeenCalledWith('', 0, 10);
    expect(linhas().length).toBe(2);
    expect(linhas()[1].textContent).toContain('bia@cefet.br');
    expect(linhas()[1].textContent).toContain('Aluno');
    expect(linhas()[1].textContent).toContain('Ativo');
  });

  it('não oferece desativar um administrador', async () => {
    await montar([adm]);
    expect(botaoDe(linhas()[0])).toBeNull();
  });

  it('desativa depois de confirmar e atualiza a linha', async () => {
    await montar([{ ...bia }]);
    confirm.confirm.and.returnValue(of(true));
    admin.desativar.and.returnValue(of({ ...bia, ativo: false }));

    botaoDe(linhas()[0])!.click();
    fixture.detectChanges();

    expect(admin.desativar).toHaveBeenCalledWith('u1');
    expect(linhas()[0].textContent).toContain('Desativado');
    expect(botaoDe(linhas()[0])!.textContent).toContain('Reativar');
    expect(feedback.success).toHaveBeenCalled();
  });

  it('cancelar a confirmação não desativa', async () => {
    await montar([{ ...bia }]);
    confirm.confirm.and.returnValue(of(false));

    botaoDe(linhas()[0])!.click();

    expect(admin.desativar).not.toHaveBeenCalled();
  });

  it('reativa sem pedir confirmação', async () => {
    await montar([{ ...bia, ativo: false }]);
    admin.reativar.and.returnValue(of({ ...bia, ativo: true }));

    botaoDe(linhas()[0])!.click();
    fixture.detectChanges();

    expect(confirm.confirm).not.toHaveBeenCalled();
    expect(admin.reativar).toHaveBeenCalledWith('u1');
    expect(linhas()[0].textContent).toContain('Ativo');
  });

  it('erro da API mostra a mensagem do servidor', async () => {
    await montar([{ ...bia }]);
    confirm.confirm.and.returnValue(of(true));
    admin.desativar.and.returnValue(
      throwError(() => ({ error: { message: 'Não é possível desativar outro administrador.' } })),
    );

    botaoDe(linhas()[0])!.click();

    expect(feedback.error).toHaveBeenCalledWith('Não é possível desativar outro administrador.');
  });

  it('buscar recarrega a primeira página com o termo', async () => {
    await montar([bia]);
    const campo = fixture.nativeElement.querySelector('input[type="search"]') as HTMLInputElement;
    campo.value = 'bia';
    campo.dispatchEvent(new Event('input'));
    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));

    expect(admin.listarUsuarios).toHaveBeenCalledWith('bia', 0, 10);
  });

  // Revisão: a paginação usa o termo da última busca feita, não o que está digitado.
  it('paginar usa o termo aplicado, não o texto digitado sem buscar', async () => {
    await montar([bia]);
    admin.listarUsuarios.and.returnValue(of({ ...pagina([bia], 2) }));
    fixture.componentInstance.carregar(0);
    fixture.detectChanges();
    const campo = fixture.nativeElement.querySelector('input[type="search"]') as HTMLInputElement;
    campo.value = 'zzz';
    campo.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const proxima = Array.from(fixture.nativeElement.querySelectorAll('.btn-pagina') as NodeListOf<HTMLButtonElement>)
      .find((b) => b.textContent!.includes('Próxima'))!;
    proxima.click();

    expect(admin.listarUsuarios).toHaveBeenCalledWith('', 1, 10);
    expect(admin.listarUsuarios).not.toHaveBeenCalledWith('zzz', jasmine.anything(), jasmine.anything());
  });

  it('resposta atrasada de uma busca antiga não sobrescreve a mais nova', async () => {
    await montar([bia]);
    const lenta = new Subject<Page<Usuario>>();
    admin.listarUsuarios.and.returnValues(lenta, of(pagina([adm])));
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;

    form.dispatchEvent(new Event('submit'));
    form.dispatchEvent(new Event('submit'));
    lenta.next(pagina([bia]));
    fixture.detectChanges();

    expect(linhas().length).toBe(1);
    expect(linhas()[0].textContent).toContain('admin@cefet-rj.br');
  });

  it('erro ao carregar oferece tentar de novo', async () => {
    await montar([bia]);
    admin.listarUsuarios.and.returnValue(throwError(() => ({ status: 500 })));
    fixture.componentInstance.carregar(0);
    fixture.detectChanges();
    admin.listarUsuarios.and.returnValue(of(pagina([bia])));

    (fixture.nativeElement.querySelector('.btn-tentar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(linhas().length).toBe(1);
  });

  it('sem resultados mostra aviso', async () => {
    await montar([]);
    expect(fixture.nativeElement.textContent).toContain('Nenhum usuário encontrado');
  });
});
