import { Component, ElementRef, ViewChild, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';
import { PostService, Post } from '../../services/post.services';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  currentView = signal<'home'>('home');
  newPostModal = signal<boolean>(false);
  modoOportunidade = signal<boolean>(false);

  tipoUsuario = signal<string>(localStorage.getItem('tipoUsuario') || 'aluno');
  usuarioLogado = signal<{ id: string; nome: string; funcao: string } | null>(null);

  posts = signal<Post[]>([]);
  oportunidades = signal<Oportunidade[]>([]);
  nomesUsuarios = new Map<string, string>();

  carregando = signal<boolean>(false);
  erro = signal<string>('');

  feedItens = computed<Post[]>(() => {
    const posts = this.posts();
    const oportunidadesMapeadas = this.oportunidades().map((op) => this.mapOportunidadeParaPost(op));
    return [...posts, ...oportunidadesMapeadas].sort(
      (a, b) => this.obterTimestamp(b.criado) - this.obterTimestamp(a.criado)
    );
  });

  titulo = signal<string>('');
  corpo = signal<string>('');
  imagemBase64 = signal<string | null>(null);
  imagemErro = signal<string>('');
  vagasTotais = signal<number>(1);
  categoria = signal<string>('');

  constructor(
    private postService: PostService,
    private oportunidadeService: OportunidadeService,
    private usuarioService: UsuarioService,
    private router: Router
  ) {}

  ngOnInit() {
    const isLogged = localStorage.getItem('isLogged') === 'true';
    if (!isLogged) {
      this.router.navigate(['/login']);
      return;
    }

    const usuario = localStorage.getItem('usuario');
    if (usuario) {
      try {
        const parsed = JSON.parse(usuario);
        this.usuarioLogado.set({
          id: parsed.id,
          nome: parsed.nome,
          funcao: parsed.funcao?.toUpperCase() || 'ALUNO'
        });
      } catch {
        this.usuarioLogado.set(null);
      }
    }

    this.carregarFeed();
  }

  private mapOportunidadeParaPost(op: Oportunidade): Post {
    const vagasInfo = `Vagas disponíveis: ${(op.vagasPreenchidas ?? 0)}/${op.quantidadeDeVagas}`;
    const descricao = op.descricao?.trim();
    const corpoFormatado = descricao ? `${vagasInfo}\n\n${descricao}` : vagasInfo;

    return {
      id: op.id,
      titulo: op.nome,
      corpo: corpoFormatado,
      criadorId: op.professorId,
      criado: op.criado ? new Date(op.criado) : undefined,
      likesId: op.idLikes ?? [],
      idComentarios: [],
      imagemBase64: op.imagemBase64,
      nomeCriador: this.nomesUsuarios.get(op.professorId || '') || 'Professor'
    };
  }

  private obterTimestamp(criado?: string | Date): number {
    if (!criado) return 0;
    if (criado instanceof Date) return criado.getTime();
    const data = new Date(criado);
    return Number.isNaN(data.getTime()) ? 0 : data.getTime();
  }

  private carregarFeed() {
    this.carregando.set(true);
    this.erro.set('');
    forkJoin({
      posts: this.postService.getPosts(),
      oportunidades: this.oportunidadeService.listar(),
      usuarios: this.usuarioService.listar()
    }).subscribe({
      next: ({ posts, oportunidades, usuarios }) => {
        this.popularCacheUsuarios(usuarios);

        const postsComNome = (posts ?? []).map((p) => ({
          ...p,
          nomeCriador: this.nomesUsuarios.get(p.criadorId || '') || 'Usuário Anônimo'
        }));

        this.posts.set(postsComNome);
        this.oportunidades.set(oportunidades ?? []);
        this.carregando.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar conteúdo:', err);
        this.erro.set('Erro ao carregar publicações.');
        this.carregando.set(false);
      }
    });
  }

  private popularCacheUsuarios(usuarios: Usuario[]) {
    this.nomesUsuarios.clear();
    for (const u of usuarios) {
      const id = u.id?.toString();
      if (id) this.nomesUsuarios.set(id, u.nome);
    }
  }

  openNewPost() {
    this.newPostModal.set(true);
  }

  closeNewPost() {
    this.newPostModal.set(false);
  }

  onImagemSelecionada(event: Event) {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];

    if (!arquivo) {
      this.imagemBase64.set(null);
      return;
    }

    if (!arquivo.type.startsWith('image/')) {
      this.imagemErro.set('Selecione um arquivo de imagem válido.');
      input.value = '';
      return;
    }

    const leitor = new FileReader();
    leitor.onload = () => {
      this.imagemBase64.set(leitor.result as string);
      this.imagemErro.set('');
    };
    leitor.onerror = () => {
      console.error('Erro ao carregar a imagem do post.');
      this.imagemErro.set('Erro ao carregar a imagem. Tente novamente.');
      this.imagemBase64.set(null);
    };
    leitor.readAsDataURL(arquivo);
  }

  removerImagem(event?: Event) {
    event?.preventDefault();
    this.imagemBase64.set(null);
    this.imagemErro.set('');
    if (this.fileInput) this.fileInput.nativeElement.value = '';
  }

  onVagasChange(valor: unknown) {
    let convertido = 0;
    if (typeof valor === 'string') convertido = Number(valor.trim());
    else if (typeof valor === 'number') convertido = valor;
    else if (valor != null) convertido = Number(valor);
    if (!Number.isFinite(convertido)) convertido = 0;
    this.vagasTotais.set(Math.max(0, Math.floor(convertido)));
  }

  alternarModoOportunidade() {
    const novoValor = !this.modoOportunidade();
    this.modoOportunidade.set(novoValor);
    if (novoValor) this.removerImagem();
  }

  private resetFormulario() {
    this.titulo.set('');
    this.corpo.set('');
    this.vagasTotais.set(1);
    this.categoria.set('');
    this.removerImagem();
  }

  /** 🔹 Criação de post normal */
  enviarPost() {
    if (!this.titulo().trim()) {
      alert('Informe um título antes de enviar.');
      return;
    }

    if (this.modoOportunidade()) {
      this.enviarOportunidade();
      return;
    }

    const usuario = this.usuarioLogado();
    const novoPost: Post = {
      titulo: this.titulo(),
      corpo: this.corpo(),
      criadorId: usuario?.id || 'usuarioAnonimo',
      criado: new Date(),
      imagemBase64: this.imagemBase64() ?? undefined,
      nomeCriador: usuario?.nome || 'Usuário'
    };

    this.postService.createPost(novoPost).subscribe({
      next: (p) => {
        const postComNome = { ...p, nomeCriador: usuario?.nome || 'Usuário' };
        this.posts.update(lista => [postComNome, ...lista]);
        this.resetFormulario();
        this.newPostModal.set(false);
      },
      error: (err) => {
        console.error('Erro ao criar post:', err);
        alert('Erro ao criar post.');
      }
    });
  }

  /** 🔹 Criação de oportunidade (somente professor) */
  private enviarOportunidade() {
    const usuario = this.usuarioLogado();
    const vagasInformadas = this.vagasTotais();
    const quantidadeNormalizada = Number.isFinite(vagasInformadas)
      ? Math.floor(Math.max(0, vagasInformadas))
      : 0;

    const novaOportunidade: Oportunidade = {
      nome: this.titulo(),
      descricao: this.corpo() || undefined,
      professorId: usuario?.id || 'usuarioAnonimo',
      quantidadeDeVagas: quantidadeNormalizada,
      idCategoria: this.categoria() || undefined,
      imagemBase64: this.imagemBase64() ?? undefined
    };

    this.oportunidadeService.criar(novaOportunidade).subscribe({
      next: (oportunidade) => {
        const opComNome = {
          ...oportunidade,
          nomeCriador: usuario?.nome || 'Professor'
        };
        this.oportunidades.update(lista => [opComNome, ...lista]);
        this.resetFormulario();
        this.newPostModal.set(false);
      },
      error: (err) => {
        console.error('Erro ao criar oportunidade:', err);
        alert('Erro ao criar oportunidade.');
      }
    });
  }
}
