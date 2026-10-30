import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject, throwError } from 'rxjs';

import { Login } from './login.component';
import { AuthService } from '../../services/auth.service';
import { FeedbackService } from '../../services/feedback.service';
import { deslogar, providersDeTeste } from '../../../testing/test-providers';

describe('LoginComponent', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let auth: jasmine.SpyObj<AuthService>;
  let feedback: jasmine.SpyObj<FeedbackService>;
  let router: Router;

  const respostaLogin = {
    token: 'jwt-123',
    refreshToken: 'refresh-123',
    usuario: { id: 'u1', nome: 'Ana', email: 'ana@cefet.br', funcao: 'PROFESSOR', matricula: '1' },
  };

  beforeEach(async () => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['login', 'guardarTokens']);
    // Mantém o comportamento real de gravar os tokens, para o teste conferir o localStorage.
    auth.guardarTokens.and.callFake((r) => {
      localStorage.setItem('token', r.token);
      if (r.refreshToken) localStorage.setItem('refreshToken', r.refreshToken);
    });
    feedback = jasmine.createSpyObj<FeedbackService>('FeedbackService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        ...providersDeTeste(),
        { provide: AuthService, useValue: auth },
        { provide: FeedbackService, useValue: feedback },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture.detectChanges();
  });

  afterEach(() => deslogar());

  const enviarLogin = () => component.onLoginSubmit(new Event('submit'));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('não chama a API sem e-mail e senha', () => {
    enviarLogin();

    expect(auth.login).not.toHaveBeenCalled();
    expect(feedback.error).toHaveBeenCalledWith('Preencha e-mail e senha.');
  });

  it('login certo grava token e usuário e vai para a home', () => {
    const resposta = new Subject<any>();
    auth.login.and.returnValue(resposta.asObservable());
    component.email.set('ana@cefet.br');
    component.senha.set('segredo');

    enviarLogin();
    resposta.next(respostaLogin);

    expect(auth.login).toHaveBeenCalledWith({ email: 'ana@cefet.br', senha: 'segredo' });
    expect(localStorage.getItem('token')).toBe('jwt-123');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-123');
    expect(JSON.parse(localStorage.getItem('usuario')!).id).toBe('u1');
    expect(localStorage.getItem('tipoUsuario')).toBe('professor');
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('login errado mostra erro e libera o botão', () => {
    auth.login.and.returnValue(throwError(() => ({ status: 401 })));
    component.email.set('ana@cefet.br');
    component.senha.set('errada');

    enviarLogin();

    expect(feedback.error).toHaveBeenCalledWith('Usuário não encontrado ou senha incorreta.');
    expect(component.enviando()).toBeFalse();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('botão fica desabilitado enquanto o login está em andamento (M4)', () => {
    const resposta = new Subject<any>();
    auth.login.and.returnValue(resposta.asObservable());
    component.email.set('ana@cefet.br');
    component.senha.set('segredo');

    enviarLogin();
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector('form .btn-submit') as HTMLButtonElement;
    expect(botao.disabled).toBeTrue();
    expect(botao.textContent).toContain('Entrando...');

    enviarLogin();
    expect(auth.login).toHaveBeenCalledTimes(1);
  });

  it('cadastro recusa senhas diferentes', () => {
    component.senha.set('abc');
    component.confirmaSenha.set('xyz');

    component.onRegisterSubmit(new Event('submit'));

    expect(feedback.error).toHaveBeenCalledWith('As senhas não coincidem.');
  });
});
