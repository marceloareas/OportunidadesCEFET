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
  grandesAreas = signal<string[]>([]);

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
          funcao: parsed.funcao?.toUpperCase() || 'NADA'
        });
      } catch {
        this.usuarioLogado.set(null);
      }
    }

    this.carregarFeed();
  }

  private mapOportunidadeParaPost(op: Oportunidade): Post {
  const descricao = op.descricao?.trim();
  const corpoFormatado = descricao || '';

  return {
    id: op.id,
    titulo: op.nome,
    corpo: corpoFormatado,
    criadorId: op.professorId,
    criado: op.criado ? new Date(op.criado) : undefined,
    idComentarios: [],
    imagemBase64: op.imagemBase64,
    nomeCriador: this.nomesUsuarios.get(op.professorId || '') || 'Professor',
    ehOportunidade: true,
      finalizada: op.finalizada ?? false,
    vagasPreenchidas: op.vagasPreenchidas ?? 0,
    quantidadeDeVagas: op.quantidadeDeVagas ?? 0,
    alunosCandidatosId: op.alunosCandidatosId ?? [],
    alunosAprovadosId: op.alunosAprovadosId ?? [],
    idLikes: op.idLikes ? [...op.idLikes] : []
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
          nomeCriador: this.nomesUsuarios.get(p.criadorId || '') || 'Usuário Anônimo',
          ehOportunidade: false as any,
          idLikes: p.idLikes ?? []
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
      if ((u as any).id) this.nomesUsuarios.set((u as any).id, u.nome);
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

  onAreaChange(event: Event) {
    const checkbox = event.target as HTMLInputElement;
    const valor = checkbox.value;

    this.grandesAreas.update(lista =>
      checkbox.checked
        ? [...lista, valor]
        : lista.filter(v => v !== valor)
    );
  }

  onVagasChange(valor: unknown) {
    let convertido = 1;

    if (typeof valor === 'string') {
      const txt = valor.trim();
      if (txt === '') convertido = 1;
      else convertido = Number(txt);
    } else if (typeof valor === 'number') {
      convertido = valor;
    } else if (valor != null) {
      convertido = Number(valor as any);
    }

    if (!Number.isFinite(convertido) || Number.isNaN(convertido)) convertido = 1;
    convertido = Math.max(1, Math.floor(convertido));

    this.vagasTotais.set(convertido);
  }

  alternarModoOportunidade() {
    const novoValor = !this.modoOportunidade();
    this.modoOportunidade.set(novoValor);
    if (novoValor) {
      if (!Number.isFinite(this.vagasTotais()) || this.vagasTotais() < 1) {
        this.vagasTotais.set(1);
      }
      this.removerImagem();
    }
  }

  private resetFormulario() {
    this.titulo.set('');
    this.corpo.set('');
    this.vagasTotais.set(1);
    this.categoria.set('');
    this.removerImagem();
  }

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
      nomeCriador: usuario?.nome || 'Usuário',
      ehOportunidade: false as any
    } as any;

    this.postService.createPost(novoPost).subscribe({
      next: (p) => {
        this.posts.update(lista => [p, ...lista]);
        this.resetFormulario();
        this.newPostModal.set(false);
      },
      error: (err) => {
        console.error('Erro ao criar post:', err);
        alert('Erro ao criar post.');
      }
    });
  }

  private enviarOportunidade() {
    if (!this.titulo().trim()) {
      alert('Informe um título para a oportunidade.');
      return;
    }

    const bruto = this.vagasTotais();
    let quantidade = Number(bruto);
    if (!Number.isFinite(quantidade) || Number.isNaN(quantidade)) quantidade = 1;
    quantidade = Math.max(1, Math.floor(quantidade));

    const usuario = this.usuarioLogado();

    const categoriaSelecionada = this.categoria()?.trim();

    if (!categoriaSelecionada) {
      alert('Selecione uma categoria.');
      return;
    }

    const novaOportunidade: Oportunidade = {
      nome: this.titulo().trim(),
      descricao: this.corpo()?.trim() || undefined,
      professorId: usuario?.id!,
      quantidadeDeVagas: quantidade,
      idCategoria: categoriaSelecionada.toUpperCase(),
      grandesAreas: this.grandesAreas(),
      imagemBase64: this.imagemBase64() ?? undefined
    };

    this.oportunidadeService.criar(novaOportunidade).subscribe({
      next: (oportunidade) => {
        const opComVagas = {
          ...oportunidade,
          quantidadeDeVagas:
            Number((oportunidade as any).quantidadeDeVagas) || quantidade,
          vagasPreenchidas:
            Number((oportunidade as any).vagasPreenchidas) || 0
        } as Oportunidade;

        this.oportunidades.update(lista => [opComVagas, ...lista]);
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
