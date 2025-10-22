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

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  // estado de visualização
  currentView = signal<'home'>('home');
  newPostModal = signal<boolean>(false);
  modoOportunidade = signal<boolean>(false);

  // dados dos posts
  posts = signal<Post[]>([]);
  oportunidades = signal<Oportunidade[]>([]);
  carregando = signal<boolean>(false);
  erro = signal<string>('');

  feedItens = computed<Post[]>(() => {
    const posts = this.posts();
    const oportunidadesMapeadas = this.oportunidades().map((op) => this.mapOportunidadeParaPost(op));
    return [...posts, ...oportunidadesMapeadas].sort(
      (a, b) => this.obterTimestamp(b.criado) - this.obterTimestamp(a.criado)
    );
  });

  // formulário do modal
  titulo = signal<string>('');
  corpo = signal<string>('');
  imagemBase64 = signal<string | null>(null);
  imagemErro = signal<string>('');
  vagasTotais = signal<number>(1);
  categoria = signal<string>('');

  constructor(
    private postService: PostService,
    private oportunidadeService: OportunidadeService,
    private router: Router
  ) {}

  ngOnInit() {
    const isLogged = localStorage.getItem('isLogged') === 'true';
    if (!isLogged) {
      this.router.navigate(['/login']);
      return;
    }
    this.carregarFeed();
  }

  private mapOportunidadeParaPost(op: Oportunidade): Post {
    const vagasInfo = `Vagas disponíveis: ${(op.vagasPreenchidas ?? 0)}/${op.quantidadeDeVagas}`;
    const descricao = op.descricao?.trim();
    const corpoFormatado = descricao ? `${descricao}\n\n${vagasInfo}` : vagasInfo;

    return {
      id: op.id,
      titulo: op.nome,
      corpo: corpoFormatado,
      criadorId: op.professorId,
      criado: op.criado ? new Date(op.criado) : undefined,
      likesId: op.idLikes ?? [],
      idComentarios: [],
      imagemBase64: undefined
    };
  }

  private obterTimestamp(criado?: string | Date): number {
    if (!criado) {
      return 0;
    }
    if (criado instanceof Date) {
      return criado.getTime();
    }
    const data = new Date(criado);
    return Number.isNaN(data.getTime()) ? 0 : data.getTime();
  }

  private carregarFeed() {
    this.carregando.set(true);
    this.erro.set('');
    forkJoin({
      posts: this.postService.getPosts(),
      oportunidades: this.oportunidadeService.listar()
    }).subscribe({
      next: ({ posts, oportunidades }) => {
        this.posts.set(posts ?? []);
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

  // modal
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
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  onVagasChange(valor: unknown) {
    let convertido = 0;
    if (typeof valor === 'string') {
      convertido = Number(valor.trim());
    } else if (typeof valor === 'number') {
      convertido = valor;
    } else if (valor !== null && valor !== undefined) {
      convertido = Number(valor);
    }

    if (!Number.isFinite(convertido)) {
      convertido = 0;
    }

    this.vagasTotais.set(Math.max(0, Math.floor(convertido)));
  }

  alternarModoOportunidade() {
    const novoValor = !this.modoOportunidade();
    this.modoOportunidade.set(novoValor);
    if (novoValor) {
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

  // criação de post
  enviarPost() {
    if (!this.titulo().trim()) {
      alert('Informe um título antes de enviar.');
      return;
    }

    if (this.modoOportunidade()) {
      this.enviarOportunidade();
      return;
    }

    const novoPost: Post = {
      titulo: this.titulo(),
      corpo: this.corpo(),
      criadorId: 'usuarioFake',
      criado: new Date(),
      imagemBase64: this.imagemBase64() ?? undefined
    };

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
    const vagasInformadas = this.vagasTotais();
    const quantidadeNormalizada = Number.isFinite(vagasInformadas)
      ? Math.floor(Math.max(0, vagasInformadas))
      : 0;

    const novaOportunidade: Oportunidade = {
      nome: this.titulo(),
      descricao: this.corpo() || undefined,
      professorId: 'usuarioFake',
      quantidadeDeVagas: quantidadeNormalizada,
      idCategoria: this.categoria() || undefined
    };

    this.oportunidadeService.criar(novaOportunidade).subscribe({
      next: (oportunidade) => {
        this.oportunidades.update(lista => [oportunidade, ...lista]);
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
