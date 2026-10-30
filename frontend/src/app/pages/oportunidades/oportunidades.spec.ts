import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { OportunidadesPage } from './oportunidades';
import { Oportunidade, OportunidadeService } from '../../services/oportunidade.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';
import { SavedItemsService } from '../../services/itens-salvos.service';
import { deslogar, logarComo, providersDeTeste } from '../../../testing/test-providers';

describe('OportunidadesPage', () => {
  let fixture: ComponentFixture<OportunidadesPage>;
  let component: OportunidadesPage;
  let servico: jasmine.SpyObj<OportunidadeService>;
  let confirm: jasmine.SpyObj<ConfirmDialogService>;
  let feedback: jasmine.SpyObj<FeedbackService>;

  const aberta: Oportunidade = {
    id: 'op-1',
    nome: 'Pesquisa em IA',
    professorId: 'prof-1',
    status: 'INSCRICOES_ABERTAS',
    finalizada: false,
    quantidadeDeVagas: 2,
    vagasPreenchidas: 0,
  };
  const finalizada: Oportunidade = { ...aberta, id: 'op-2', status: 'FINALIZADA', finalizada: true };

  const pagina = (content: Oportunidade[]) =>
    of({ content, number: 0, totalPages: 1, totalElements: content.length, size: 10 } as any);

  async function montar() {
    servico = jasmine.createSpyObj<OportunidadeService>('OportunidadeService', [
      'listarPorProfessor',
      'listarPorAluno',
      'finalizarOportunidade',
    ]);
    servico.listarPorProfessor.and.returnValue(pagina([aberta, finalizada]));
    servico.listarPorAluno.and.returnValue(pagina([{ ...aberta, statusCandidaturaAluno: 'APROVADO' }]));
    confirm = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    feedback = jasmine.createSpyObj<FeedbackService>('FeedbackService', ['success', 'error']);
    const salvos = jasmine.createSpyObj<SavedItemsService>('SavedItemsService', ['estaSalvo']);
    salvos.estaSalvo.and.returnValue(of(false));

    await TestBed.configureTestingModule({
      imports: [OportunidadesPage],
      providers: [
        ...providersDeTeste(),
        { provide: OportunidadeService, useValue: servico },
        { provide: ConfirmDialogService, useValue: confirm },
        { provide: FeedbackService, useValue: feedback },
        { provide: SavedItemsService, useValue: salvos },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OportunidadesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  const botoesFinalizar = () =>
    Array.from(fixture.nativeElement.querySelectorAll('.btn-finalizar')) as HTMLButtonElement[];

  afterEach(() => deslogar());

  describe('professor', () => {
    beforeEach(async () => {
      logarComo({ id: 'prof-1', nome: 'Carlos', funcao: 'PROFESSOR' });
      await montar();
    });

    it('lista as próprias oportunidades', () => {
      expect(servico.listarPorProfessor).toHaveBeenCalledWith('prof-1', 0, 10);
      expect(component.minhasOportunidades().length).toBe(2);
    });

    it('mostra "Finalizar oportunidade" só nas que estão abertas', () => {
      expect(botoesFinalizar().length).toBe(1);
      expect(botoesFinalizar()[0].textContent).toContain('Finalizar oportunidade');
    });

    it('finaliza depois de confirmar e atualiza a lista', () => {
      confirm.confirm.and.returnValue(of(true));
      servico.finalizarOportunidade.and.returnValue(of({ ...aberta, status: 'FINALIZADA', finalizada: true }));

      botoesFinalizar()[0].click();
      fixture.detectChanges();

      expect(servico.finalizarOportunidade).toHaveBeenCalledWith('op-1');
      expect(component.minhasOportunidades()[0].finalizada).toBeTrue();
      expect(botoesFinalizar().length).toBe(0);
      expect(feedback.success).toHaveBeenCalledWith('Oportunidade finalizada com sucesso!');
    });

    it('não finaliza se o professor cancelar', () => {
      confirm.confirm.and.returnValue(of(false));

      botoesFinalizar()[0].click();

      expect(servico.finalizarOportunidade).not.toHaveBeenCalled();
    });

    it('mostra a mensagem do backend quando a finalização falha', () => {
      confirm.confirm.and.returnValue(of(true));
      servico.finalizarOportunidade.and.returnValue(
        throwError(() => ({ status: 409, error: { message: 'Todas as vagas já foram preenchidas.' } })),
      );

      botoesFinalizar()[0].click();

      expect(feedback.error).toHaveBeenCalledWith('Todas as vagas já foram preenchidas.');
      expect(component.finalizandoId()).toBeNull();
    });
  });

  describe('aluno', () => {
    beforeEach(async () => {
      logarComo({ id: 'aluno-1', nome: 'Bia', funcao: 'ALUNO' });
      await montar();
    });

    it('lista as próprias candidaturas com o status', () => {
      expect(servico.listarPorAluno).toHaveBeenCalledWith('aluno-1', 0, 10);
      expect(component.candidaturas()[0].statusCandidaturaAluno).toBe('APROVADO');
      expect(component.statusAluno(component.candidaturas()[0])).toBe('Aprovado');
    });

    it('não vê ações de professor', () => {
      expect(botoesFinalizar().length).toBe(0);
      expect(fixture.nativeElement.querySelector('.btn-nova-oportunidade')).toBeNull();
    });
  });
});
