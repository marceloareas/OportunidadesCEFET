import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';

import { PostComponent } from './post';
import { FeedItem } from '../../services/feed.service';
import { Oportunidade, OportunidadeService } from '../../services/oportunidade.service';
import { SavedItemsService } from '../../services/itens-salvos.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';
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
  });
});
