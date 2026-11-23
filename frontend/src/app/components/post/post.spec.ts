import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';

import { PostComponent } from './post';
import { FeedItem } from '../../services/feed.service';
import { Oportunidade, OportunidadeService } from '../../services/oportunidade.service';
import { SavedItemsService } from '../../services/itens-salvos.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';
import { PostService } from '../../services/post.services';
import { ComentarioService } from '../../services/comentario.service';
import { deslogar, logarComo, providersDeTeste } from '../../../testing/test-providers';

describe('PostComponent', () => {
  let fixture: ComponentFixture<PostComponent>;
  let component: PostComponent;
  let oportunidades: jasmine.SpyObj<OportunidadeService>;
  let confirm: jasmine.SpyObj<ConfirmDialogService>;
  let feedback: jasmine.SpyObj<FeedbackService>;

  const oportunidadeAberta = (extra: Partial<FeedItem> = {}): FeedItem =>
    ({
      id: 'f1',
      referenciaId: 'op-1',
      tipo: 'OPORTUNIDADE',
      titulo: 'Monitoria de Cálculo',
      status: 'INSCRICOES_ABERTAS',
      quantidadeDeVagas: 2,
      vagasPreenchidas: 0,
      alunosCandidatosId: [],
      ...extra,
    }) as FeedItem;

  async function montar(post: FeedItem) {
    oportunidades = jasmine.createSpyObj<OportunidadeService>('OportunidadeService', [
      'candidatarAluno',
      'curtirOportunidade',
      'deletar',
    ]);
    confirm = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    feedback = jasmine.createSpyObj<FeedbackService>('FeedbackService', ['success', 'error']);
    const salvos = jasmine.createSpyObj<SavedItemsService>('SavedItemsService', ['estaSalvo']);
    salvos.estaSalvo.and.returnValue(of(false));

    await TestBed.configureTestingModule({
      imports: [PostComponent],
      providers: [
        ...providersDeTeste(),
        { provide: OportunidadeService, useValue: oportunidades },
        { provide: ConfirmDialogService, useValue: confirm },
        { provide: FeedbackService, useValue: feedback },
        { provide: SavedItemsService, useValue: salvos },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostComponent);
    component = fixture.componentInstance;
    component.post = post;
    fixture.detectChanges();
  }

  const botaoCandidatar = () =>
    fixture.nativeElement.querySelector('.btn-candidatar') as HTMLButtonElement | null;
  const botaoRemover = () =>
    fixture.nativeElement.querySelector('.btn-remover-post') as HTMLButtonElement | null;

  afterEach(() => deslogar());

  describe('como aluno', () => {
    beforeEach(() => logarComo({ id: 'aluno-1', nome: 'Bia', funcao: 'ALUNO' }));

    it('mostra o botão CANDIDATAR-SE em oportunidade aberta', async () => {
      await montar(oportunidadeAberta());

      expect(botaoCandidatar()?.disabled).toBeFalse();
      expect(botaoCandidatar()?.textContent).toContain('CANDIDATAR-SE');
    });

    it('confirma, envia a candidatura e marca como candidatado', async () => {
      await montar(oportunidadeAberta());
      confirm.confirm.and.returnValue(of(true));
      const resposta = new Subject<Oportunidade>();
      oportunidades.candidatarAluno.and.returnValue(resposta.asObservable());

      botaoCandidatar()!.click();
      fixture.detectChanges();

      expect(oportunidades.candidatarAluno).toHaveBeenCalledWith('op-1', 'aluno-1');
      expect(botaoCandidatar()!.disabled).toBeTrue();
      expect(botaoCandidatar()!.textContent).toContain('ENVIANDO...');

      resposta.next({ id: 'op-1', nome: 'Monitoria de Cálculo' });
      fixture.detectChanges();

      expect(component.jaCandidatado()).toBeTrue();
      expect(component.contadorCandidatos()).toBe(1);
      expect(botaoCandidatar()!.textContent).toContain('CANDIDATADO');
      expect(feedback.success).toHaveBeenCalledWith('Candidatura enviada com sucesso!');
    });

    it('cancelar a confirmação não envia nada', async () => {
      await montar(oportunidadeAberta());
      confirm.confirm.and.returnValue(of(false));

      botaoCandidatar()!.click();

      expect(oportunidades.candidatarAluno).not.toHaveBeenCalled();
    });

    it('erro na API mostra mensagem e libera o botão', async () => {
      await montar(oportunidadeAberta());
      confirm.confirm.and.returnValue(of(true));
      oportunidades.candidatarAluno.and.returnValue(throwError(() => ({ status: 409 })));

      botaoCandidatar()!.click();
      fixture.detectChanges();

      expect(feedback.error).toHaveBeenCalledWith('Erro ao se candidatar à vaga.');
      expect(component.candidaturaEnviando()).toBeFalse();
      expect(component.jaCandidatado()).toBeFalse();
    });

    it('já candidatado vê o botão desabilitado', async () => {
      await montar(oportunidadeAberta({ alunosCandidatosId: ['aluno-1'] }));

      expect(botaoCandidatar()!.disabled).toBeTrue();
      expect(botaoCandidatar()!.textContent).toContain('CANDIDATADO');
    });

    it('oportunidade finalizada não aceita candidatura', async () => {
      await montar(oportunidadeAberta({ status: 'FINALIZADA', finalizada: true }));

      expect(botaoCandidatar()!.disabled).toBeTrue();
      expect(component.podeCandidatar()).toBeFalse();
    });
  });

  describe('como professor', () => {
    beforeEach(() => logarComo({ id: 'prof-1', nome: 'Carlos', funcao: 'PROFESSOR' }));

    it('não vê o botão de candidatura', async () => {
      await montar(oportunidadeAberta());

      expect(botaoCandidatar()).toBeNull();
    });

    it('não vê o botão de remover postagem', async () => {
      await montar(oportunidadeAberta());

      expect(botaoRemover()).toBeNull();
    });
  });

  // Sprint 6: moderação de postagens e comentários.
  describe('como administrador', () => {
    beforeEach(() => logarComo({ id: 'adm', nome: 'Administrador', funcao: 'ADMIN' }));

    const discussao = (): FeedItem =>
      ({ id: 'f2', referenciaId: 'p-1', tipo: 'POST', titulo: 'Dúvida', criadorId: 'aluno-1' }) as FeedItem;

    it('remove uma discussão depois de confirmar e avisa a página', async () => {
      await montar(discussao());
      const posts = TestBed.inject(PostService);
      spyOn(posts, 'deletar').and.returnValue(of(void 0));
      confirm.confirm.and.returnValue(of(true));
      const removidos: string[] = [];
      component.removido.subscribe((id) => removidos.push(id));

      botaoRemover()!.click();

      expect(posts.deletar).toHaveBeenCalledWith('p-1');
      expect(removidos).toEqual(['p-1']);
      expect(feedback.success).toHaveBeenCalled();
    });

    it('remove uma oportunidade pela rota de oportunidades', async () => {
      await montar(oportunidadeAberta());
      oportunidades.deletar.and.returnValue(of(void 0));
      confirm.confirm.and.returnValue(of(true));

      botaoRemover()!.click();

      expect(oportunidades.deletar).toHaveBeenCalledWith('op-1');
    });

    it('cancelar não remove nada', async () => {
      await montar(oportunidadeAberta());
      confirm.confirm.and.returnValue(of(false));

      botaoRemover()!.click();

      expect(oportunidades.deletar).not.toHaveBeenCalled();
    });

    it('não vê o botão de candidatura', async () => {
      await montar(oportunidadeAberta());

      expect(botaoCandidatar()).toBeNull();
    });

    it('apaga um comentário e atualiza o contador', async () => {
      await montar(discussao());
      const comentarios = TestBed.inject(ComentarioService);
      spyOn(comentarios, 'deletar').and.returnValue(of(void 0));
      confirm.confirm.and.returnValue(of(true));
      component.comentarios = [
        { id: 'c1', autor: 'Bia', texto: 'ofensivo' },
        { id: 'c2', autor: 'Caio', texto: 'ok' },
      ];
      component.contadorComentarios.set(2);
      component.newCommentModal.set(true);
      fixture.detectChanges();

      (fixture.nativeElement.querySelector('.btn-remover-comentario') as HTMLButtonElement).click();

      expect(comentarios.deletar).toHaveBeenCalledWith('c1');
      expect(component.comentarios.map((c) => c.id)).toEqual(['c2']);
      expect(component.contadorComentarios()).toBe(1);
    });
  });

  describe('como aluno, nos comentários', () => {
    beforeEach(() => logarComo({ id: 'aluno-1', nome: 'Bia', funcao: 'ALUNO' }));

    it('não vê a lixeira de comentário nem o remover postagem', async () => {
      await montar(oportunidadeAberta());
      component.comentarios = [{ id: 'c1', autor: 'Caio', texto: 'oi' }];
      component.newCommentModal.set(true);
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.btn-remover-comentario')).toBeNull();
      expect(botaoRemover()).toBeNull();
    });
  });
});
