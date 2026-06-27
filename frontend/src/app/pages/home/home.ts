import { Component, ElementRef, ViewChild, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';
import { CandidatosModal } from '../../components/candidatos-modal/candidatos-modal';
import { PostService, Post } from '../../services/post.services';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { FeedItem, FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent, CandidatosModal],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  currentView = signal<'home'>('home');
  newPostModal = signal<boolean>(false);
  modoOportunidade = signal<boolean>(false);

  tipoUsuario = signal<string>('aluno');
  usuarioLogado = signal<{ id: string; nome: string; funcao: string; imagemPerfil?: string } | null>(null);

  feedItens = signal<FeedItem[]>([]);
  page = signal<number>(0);
  size = 10;
  hasMore = signal<boolean>(true);

  // filtros de oportunidades (enviados ao backend; ver carregarFeed)
  filtroStatus = signal<string>('');
  filtroCategoria = signal<string>('');
  filtroArea = signal<string>('');

  filtrosAtivos = computed(() =>
    Boolean(this.filtroStatus() || this.filtroCategoria() || this.filtroArea())
  );

  carregando = signal<boolean>(false);
  erro = signal<string>('');

  // modal de candidatos (professor vendo candidatos de uma oportunidade do feed)
  mostrandoModalCandidatos = signal<boolean>(false);
  oportunidadeSelecionadaId = signal<string | null>(null);
  professorSelecionadoId = signal<string | null>(null);
  oportunidadeSelecionadaFinalizada = signal<boolean>(false);

  titulo = signal<string>('');
  corpo = signal<string>('');
  imagemBase64 = signal<string | null>(null);
  imagemErro = signal<string>('');
  vagasTotais = signal<number>(1);
  dataInicioInscricao = signal<string>('');
  dataFimInscricao = signal<string>('');
  categoria = signal<string>('');
  grandesAreas = signal<string[]>([]);

  constructor(
    private postService: PostService,
    private oportunidadeService: OportunidadeService,
    private feedService: FeedService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  trackByFeedId(index: number, item: FeedItem): string {
    return item.id!;
  }

  // Chamado quando qualquer filtro muda: recarrega o feed do backend já filtrado.
  aplicarFiltros() {
    this.carregarFeed(true);
  }

  limparFiltros() {
    this.filtroStatus.set('');
    this.filtroCategoria.set('');
    this.filtroArea.set('');
    this.carregarFeed(true);
  }

  ngOnInit() {
    // Verificar se está no browser (não no SSR)
    if (typeof window === 'undefined') {
      return;
    }

    const token = localStorage.getItem('token');
    const usuario = localStorage.getItem('usuario');

    if (!token || !usuario) {
      this.router.navigate(['/login']);
      return;
    }

    try {
      const parsed = JSON.parse(usuario);
      this.usuarioLogado.set({
        id: parsed.id,
        nome: parsed.nome,
        funcao: parsed.funcao?.toUpperCase() || 'NADA',
        imagemPerfil: parsed.imagemPerfil
      });

      const tipoUsuario = localStorage.getItem('tipoUsuario') || 'aluno';
      this.tipoUsuario.set(tipoUsuario);

      if (this.route.snapshot.queryParamMap.get('modo') === 'oportunidade') {
        this.modoOportunidade.set(true);
        this.newPostModal.set(true);
      }
    } catch {
      this.router.navigate(['/login']);
      return;
    }

    this.carregarFeed();
  }

  private carregarFeed(reset = true) {
    if (reset) {
      this.page.set(0);
      this.feedItens.set([]);
      this.hasMore.set(true);
    }

    this.carregando.set(true);
    this.erro.set('');

    const filtros = {
      status: this.filtroStatus() || undefined,
      categoria: this.filtroCategoria() || undefined,
      area: this.filtroArea() || undefined,
      userId: this.usuarioLogado()?.id,
    };

    this.feedService.listar(this.page(), this.size, filtros).subscribe({
      next: (res) => {
        this.feedItens.update(lista => [...lista, ...res.content]);

        this.hasMore.set(this.page() + 1 < res.totalPages);
        this.page.set(this.page() + 1);

        this.carregando.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar feed:', err);
        this.erro.set('Erro ao carregar publicações.');
        this.carregando.set(false);
      }
    });
  }

  carregarMais() {
    if (this.carregando() || !this.hasMore()) return;
    this.carregarFeed(false);
  }

  abrirModalCandidatos(item: FeedItem) {
    const refId = item.referenciaId || item.id;
    if (!refId) return;

    this.oportunidadeSelecionadaId.set(refId);
    this.professorSelecionadoId.set(item.criadorId ?? null);
    this.oportunidadeSelecionadaFinalizada.set(
      Boolean(item.finalizada) || item.status === 'FINALIZADA'
    );
    this.mostrandoModalCandidatos.set(true);
  }

  fecharModalCandidatos() {
    this.mostrandoModalCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
    this.professorSelecionadoId.set(null);
    this.oportunidadeSelecionadaFinalizada.set(false);
  }

  aoAprovarCandidatos(oportunidade: Oportunidade) {
    this.feedItens.update(lista =>
      lista.map(item => {
        const refId = item.referenciaId || item.id;
        return refId === oportunidade.id
          ? { ...item, ...this.oportunidadeComoAtualizacaoFeed(oportunidade) }
          : item;
      })
    );
  }

  private oportunidadeComoAtualizacaoFeed(op: Oportunidade): Partial<FeedItem> {
    return {
      vagasPreenchidas: op.vagasPreenchidas,
      finalizada: op.finalizada,
      status: op.status,
      alunosAprovadosId: op.alunosAprovadosId,
      alunosCandidatosId: op.alunosCandidatosId,
    };
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
        ? Array.from(new Set([...lista, valor]))
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
    this.dataInicioInscricao.set('');
    this.dataFimInscricao.set('');
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
      createdAt: new Date(),
      imagemBase64: this.imagemBase64() ?? undefined,
      nomeCriador: usuario?.nome || 'Usuário',
    } as any;

    this.postService.createPost(novoPost).subscribe({
      next: (p) => {
        this.carregarFeed(true);
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

    const dataInicio = this.dataInicioInscricao().trim();
    const dataFim = this.dataFimInscricao().trim();

    if (!dataInicio || !dataFim) {
      alert('Informe a data inicial e final das inscrições.');
      return;
    }

    const inicio = new Date(`${dataInicio}T00:00:00`);
    const fim = new Date(`${dataFim}T23:59:59`);

    if (Number.isNaN(inicio.getTime()) || Number.isNaN(fim.getTime())) {
      alert('Informe um período de inscrição válido.');
      return;
    }

    if (fim < inicio) {
      alert('A data final das inscrições deve ser igual ou posterior à data inicial.');
      return;
    }

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
      imagemBase64: this.imagemBase64() ?? undefined,
      dataInicioInscricao: inicio.toISOString(),
      dataFimInscricao: fim.toISOString()
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

        this.carregarFeed(true);
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
