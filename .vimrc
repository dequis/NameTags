" vim config
" mostly shit stolen from other places

" sanity
set nocompatible
set magic
set fileencoding=utf-8
set nobackup
set visualbell t_vb=""

" other basic settings
set background=dark
set dir=~/.vim/tmp
syntax on

" default indentation settings
set softtabstop=4
set shiftwidth=4
set expandtab
set backspace=indent,eol,start
filetype plugin indent on
set list listchars=tab:»\ ,trail:·

" wildmenu
set wildmode=longest:full
set wildmenu

" search related stuff
set showmatch
set incsearch
set hlsearch

" clear hlssearch with \n
nmap <silent> <leader>n :silent :nohlsearch<CR>

" avoid slowdowns
set synmaxcol=400

" use the X clipboard if possible
set clipboard=unnamedplus

" virtualedit
set virtualedit=block
noremap j gj
noremap k gk

" goddamn capitals
cabbr W w
cabbr Q q
cabbr wQ wq
cabbr WQ wq
cabbr Wq wq

" indentation styles for different formats
"au FileType xhtml setlocal sts=2 ts=2 sw=2
"au FileType html setlocal sts=2 ts=2 sw=2
"au FileType htmldjango setlocal sts=2 ts=2 sw=2
"au FileType c setlocal sts=0 ts=8 sw=8 noexpandtab cindent
"au FileType cpp setlocal sts=0 ts=8 sw=8 noexpandtab

" colors
set t_Co=256
colorscheme wombat256roger

" really really bright highlights of stuff i shouldn't push
highlight XXXDX ctermfg=black ctermbg=red 
let g:xxxdx = matchadd("XXXDX", "XXXDX")

" highlight after 80 cols (only for python code)
highlight ExtraWhitespace ctermbg=215 ctermfg=black
"highlight ExtraWhitespace2 ctermbg=202 ctermfg=black

function! HighlightAfter80Cols()
    let w:m1=matchadd('ExtraWhitespace', '\%<81v.\%>80v', -1)
    "let w:m2=matchadd('ExtraWhitespace2', '\%>80v.\+', -1)
    "let w:m3=matchadd('ExtraWhitespace2', '\%>80v.\+', -1)
endfunction

au FileType python call HighlightAfter80Cols() 

" SudoEdit.vim
noremap ss :w sudo:%<CR>

" matchit (builtin)
runtime macros/matchit.vim

" nerdtree
map <F2> :NERDTreeToggle<CR>
let NERDTreeIgnore=['\.pyc$']

" syntastic
let g:syntastic_python_checkers = ['pyflakes']

" localrc
let g:localrc_filename = '.vim.custom'

" vala 
autocmd BufRead *.vala,*.vapi set efm=%f:%l.%c-%[%^:]%#:\ %t%[%^:]%#:\ %m
au BufRead,BufNewFile *.vala,*.vapi setfiletype vala

" vim-twig sucks
au BufRead,BufNewFile *.twig setfiletype htmldjango

" recover plugin
let g:RecoverPlugin_Edit_Unmodified = 1

" pathogen
execute pathogen#infect()
